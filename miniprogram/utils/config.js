// 后端服务地址。
// 真机调试时，请把 localhost 改成电脑的局域网 IP（例如 http://192.168.1.100:8080），
// 并确保手机与电脑处于同一 WiFi 下。
const BASE_URL = 'http://localhost:8080';

// 拼接图片完整地址：
// - path 为空返回空字符串；
// - path 以 http 开头（绝对地址）直接返回；
// - 否则返回 BASE_URL + path。
function imgUrl(path) {
  if (!path) {
    return '';
  }
  if (path.indexOf('http') === 0) {
    return path;
  }
  return BASE_URL + path;
}

module.exports = {
  BASE_URL,
  imgUrl,
};