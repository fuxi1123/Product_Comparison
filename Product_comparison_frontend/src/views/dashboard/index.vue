<template>
  <div class="dashboard-container">
    <h1 class="dashboard-title">商品比价系统</h1>
  </div>
</template>

<script>
export default {
  name: '主页'
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 94vh; /* 让容器充满整个视口高度 */
  background-color: #f5f5f5; /* 可选：添加背景色 */
}

.dashboard-title {
  font-size: 60px; 
  font-weight: bold; /* 标题加粗 */
  color: #333; /* 标题颜色 */
}
</style>
