const { post } = require('./utils/request');

App({
  globalData: {
    userInfo: null,
  },

  onLaunch() {
    this.ensureLogin();
  },

  // 无 token 时静默登录，失败不影响启动。
  ensureLogin() {
    const token = wx.getStorageSync('token');
    if (token) {
      const user = wx.getStorageSync('userInfo');
      if (user) {
        this.globalData.userInfo = user;
      }
      return;
    }

    // 生成稳定的设备码，写入 storage 以便下次复用。
    let deviceCode = wx.getStorageSync('deviceCode');
    if (!deviceCode) {
      deviceCode = 'user_' + Date.now() + '_' + Math.floor(Math.random() * 100000);
      wx.setStorageSync('deviceCode', deviceCode);
    }

    post('/api/user/login', { code: deviceCode, nickname: '校园用户' })
      .then((data) => {
        const user = data || {};
        if (user.token) {
          wx.setStorageSync('token', user.token);
        }
        wx.setStorageSync('userInfo', user);
        this.globalData.userInfo = user;
      })
      .catch(() => {
        // 静默失败，不影响启动。
      });
  },
});