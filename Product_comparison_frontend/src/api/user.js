import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export function getInfo(token) {
  return request({
    url: '/user/info',
    method: 'get',
    params: { token }
  })
}

export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

export function register(data) {
  return request({
    url: '/user/register', // 后端注册接口
    method: 'post',
    data
  });
}

export function checkUsername(username) {
  return request({
    url: '/user/check-username', 
    method: 'get',
    params: { username } // 使用查询参数传递用户名
  });
}

export function checkEmail(email) {
  return request({
    url: '/user/check-email',
    method: 'get',
    params: { email } // 使用查询参数传递邮箱
  });
}

