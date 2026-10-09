const { BASE_URL } = require('./config');

// 统一封装 wx.request，返回 Promise。
function request(method, url, data) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    const header = {
      'Content-Type': 'application/json',
    };
    if (token) {
      header.Authorization = 'Bearer ' + token;
    }

    wx.request({
      url: BASE_URL + url,
      method,
      data,
      header,
      success(res) {
        const body = res.data;
        if (body && body.code === 200) {
          resolve(body.data);
        } else {
          const msg = (body && body.message) || '请求失败';
          wx.showToast({ title: msg, icon: 'none' });
          reject(body || new Error(msg));
        }
      },
      fail() {
        wx.showToast({ title: '网络异常', icon: 'none' });
        reject(new Error('网络异常'));
      },
    });
  });
}

function get(url, data) {
  return request('GET', url, data);
}

function post(url, data) {
  return request('POST', url, data);
}

function put(url, data) {
  return request('PUT', url, data);
}

function del(url, data) {
  return request('DELETE', url, data);
}

// 图片上传：multipart 表单，字段名为 file，返回图片相对路径。
function upload(filePath) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    const header = {};
    if (token) {
      header.Authorization = 'Bearer ' + token;
    }

    wx.uploadFile({
      url: BASE_URL + '/api/upload/image',
      filePath,
      name: 'file',
      header,
      success(res) {
        try {
          const body = JSON.parse(res.data);
          if (body && body.code === 200) {
            resolve(body.data);
          } else {
            const msg = (body && body.message) || '上传失败';
            wx.showToast({ title: msg, icon: 'none' });
            reject(body || new Error(msg));
          }
        } catch (e) {
          wx.showToast({ title: '上传失败', icon: 'none' });
          reject(e);
        }
      },
      fail() {
        wx.showToast({ title: '网络异常', icon: 'none' });
        reject(new Error('网络异常'));
      },
    });
  });
}

module.exports = {
  get,
  post,
  put,
  del,
  upload,
};