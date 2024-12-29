<template>
  <div class="alert-page">
    <el-card>
      <h2>我的提醒</h2>
      <el-table :data="alerts" style="width: 100%">
        <!-- 商品名称 -->
        <el-table-column
          prop="product.title"
          label="商品名称"
        >
          <template #default="scope">
            <el-link :href="scope.row.product.link" target="_blank">
              {{ scope.row.product.title }}
            </el-link>
          </template>
        </el-table-column>

        <!-- 当前价格 -->
        <el-table-column
          prop="product.price"
          label="当前价格 (¥)"
        >
          <template #default="scope">
            {{ scope.row.product.price }}
          </template>
        </el-table-column>

        <!-- 提醒时价格 -->
        <el-table-column
          prop="alert.initialPrice"
          label="添加提醒时价格 (¥)"
        >
          <template #default="scope">
            {{ scope.row.alert.initialPrice }}
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作">
          <template #default="scope">
            <el-button
              type="danger"
              size="mini"
              @click="deleteAlert(scope.row.alert.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getPriceAlerts, deletePriceAlert } from "@/api/alert";

export default {
  data() {
    return {
      alerts: [], // 存储提醒列表
    };
  },
  methods: {
    // 获取提醒列表
    async fetchAlerts() {
      try {
        // 从 Vuex 或 LocalStorage 获取 userId
        const userId = this.$store.state.user.id;

        if (!userId) {
          this.$message.error("无法获取用户 ID，请重新登录！");
          return;
        }

        const response = await getPriceAlerts(userId); // 调用 API
        if (response.code === 20000) {
          this.alerts = response.data.alerts; // 更新提醒列表
        } else {
          this.$message.error(response.message || "获取提醒失败");
        }
      } catch (error) {
        console.error("Error fetching alerts:", error);
        this.$message.error("获取提醒失败");
      }
    },
    // 删除提醒
    async deleteAlert(id) {
      try {
        await deletePriceAlert(id);
        this.$message.success("提醒删除成功");
        this.fetchAlerts(); // 刷新提醒列表
      } catch (error) {
        console.error("Error deleting alert:", error);
        this.$message.error("提醒删除失败");
      }
    },
  },
  mounted() {
    this.fetchAlerts(); // 页面加载时获取提醒列表
  },
};
</script>

<style scoped>
.alert-page {
  padding: 20px;
}
</style>
