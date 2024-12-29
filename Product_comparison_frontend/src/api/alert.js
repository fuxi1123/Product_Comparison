// src/api/alert.js
import request from '@/utils/request'

// 创建提醒
export function createPriceAlert(data) {
  return request({
    url: '/alert',
    method: 'post',
    data,
  })
}

// 删除提醒
export function deletePriceAlert(id) {
  return request({
    url: `/alert/${id}`,
    method: 'delete',
  })
}

// 获取用户提醒列表
export function getPriceAlerts(userId) {

  return request({
    url: `/alert/getalert/${userId}`, // 将 userId 放入路径中
    method: "get",
  });
}


