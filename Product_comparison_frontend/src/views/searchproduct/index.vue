<template>
    <div class="search-container">
      <div class="search-header">
        <el-input
          v-model="query"
          placeholder="请输入商品关键词"
          class="search-input"
          clearable
        />
        <el-button
          type="primary"
          @click="fetchProducts"
          :loading="loading"
          class="search-button"
        >
          查询
        </el-button>
      </div>
  
      <!-- 表格容器 -->
      <div v-if="!loading && products.length > 0" class="table-container">
        <el-table
          :data="paginatedProducts"
          border
          stripe
          style="width: 100%; max-width: 950px; margin: 0 auto;"
        >
          <el-table-column prop="title" label="商品名称" width="400">
            <template #default="scope">
              <el-link :href="scope.row.link" target="_blank">
                {{ scope.row.title }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格 (¥)" width="150">
            <template #default="scope">
              <span>{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.source === '淘宝' ? 'success' : 'info'">
                {{ scope.row.source }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="image" label="图片" width="150">
            <template #default="scope">
              <el-image
                :src="scope.row.image"
                fit="contain"
                style="width: 100px; height: 100px;"
              />
            </template>
          </el-table-column>
          <!-- 新增历史价格列 -->
          <el-table-column label="历史价格" width="150">
            <template #default="scope">
              <el-button
                type="text"
                size="small"
                @click="viewPriceHistory(scope.row)"
              >
                查看历史价格
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
  
      <!-- 分页器 -->
      <div
        v-if="!loading && products.length > pageSize"
        class="pagination-container"
      >
        <el-pagination
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="products.length"
          layout="prev, pager, next"
          class="pagination"
        />
      </div>
  
      <!-- 无数据时显示 -->
      <div v-if="!loading && products.length === 0" class="no-data">
        <el-empty description="暂无商品信息，请输入关键词查询" />
      </div>
  
  
      <!-- 历史价格折线图弹窗 -->
      <el-dialog
        :visible.sync="historyDialogVisible"
        title="历史价格走势"
        width="60%"
      >
        <div v-if="priceHistory.length > 0">
          <canvas id="priceHistoryChart"></canvas>
        </div>
        <div v-else>
          <p>暂无历史价格数据</p>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="historyDialogVisible = false">关闭</el-button>
        </div>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import axios from "axios";
  import { Chart } from "chart.js";
  
  export default {
    name: "SearchProduct",
    data() {
      return {
        query: "",
        products: [],
        loading: false,
        dialogVisible: false,
        selectedProduct: null,
        currentPage: 1,
        pageSize: 10,
        historyDialogVisible: false,
        priceHistory: [],
        chartInstance: null, // 保存图表实例
      };
    },
    computed: {
      paginatedProducts() {
        const start = (this.currentPage - 1) * this.pageSize;
        const end = start + this.pageSize;
        return this.products
          .sort((a, b) => parseFloat(a.price) - parseFloat(b.price))
          .slice(start, end);
      },
    },
    methods: {
      async fetchProducts() {
        if (!this.query.trim()) {
          this.$message.warning("请输入查询关键词！");
          return;
        }
  
        this.loading = true;
        try {
          const response = await axios.get("http://localhost:80/product/scrape", {
            params: { query: this.query },
          });
          if (response.data.success) {
            this.products = response.data.data.products;
          } else {
            this.$message.error("获取商品信息失败，请稍后重试！");
          }
        } catch (error) {
          this.$message.error("获取商品信息失败，请稍后重试！");
          console.error("Error fetching products:", error);
        } finally {
          this.loading = false;
        }
      },
      viewDetails(product) {
        this.selectedProduct = product;
        this.dialogVisible = true;
      },
      handleCurrentChange(page) {
        this.currentPage = page;
      },
      async viewPriceHistory(product) {
        // 清空数据防止上次残留
        this.priceHistory = [];
        this.historyDialogVisible = false;
  
        try {
          const response = await axios.get(
            "http://localhost:80/product/history",
            { params: { title: product.title } }
          );
          if (response.data.success) {
            this.priceHistory = response.data.data.history;
            this.historyDialogVisible = true;
            this.$nextTick(() => {
              this.renderPriceHistoryChart(product.title);
            });
          } else {
            this.$message.error("获取历史价格数据失败");
          }
        } catch (error) {
          this.$message.error("获取历史价格数据失败，请稍后重试！");
          console.error("Error fetching price history:", error);
        }
      },
      renderPriceHistoryChart(title) {
        const chartElement = document.getElementById("priceHistoryChart");
        if (!chartElement) return;
  
        const ctx = chartElement.getContext("2d");
  
        // 销毁旧的图表实例
        if (this.chartInstance) {
          this.chartInstance.destroy();
        }
  
        const labels = this.priceHistory.map((item) => item.date);
        const prices = this.priceHistory.map((item) => parseFloat(item.price));
  
        this.chartInstance = new Chart(ctx, {
          type: "line",
          data: {
            labels,
            datasets: [
              {
                label: `历史价格 (¥)`,
                data: prices,
                borderColor: "rgba(75, 192, 192, 1)",
                borderWidth: 2,
                fill: false,
              },
            ],
          },
          options: {
            responsive: true,
            plugins: {
              legend: {
                position: "top",
              },
            },
            scales: {
              x: {
                title: {
                  display: true,
                  text: "日期",
                },
              },
              y: {
                title: {
                  display: true,
                  text: "价格 (¥)",
                },
                beginAtZero: false,
              },
            },
          },
        });
      },
    },
  };
  </script>
  
  <style scoped>
  .search-container {
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    text-align: center;
  }
  
  .search-header {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .search-input {
    width: 300px;
    margin-right: 10px;
  }
  
  .search-button {
    flex-shrink: 0;
  }
  
  .table-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin: 20px 0;
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin: 20px 0;
  }
  
  .no-data {
    margin-top: 20px;
    text-align: center;
  }
  </style>
  