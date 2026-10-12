const { get } = require('../../utils/request');
const { imgUrl } = require('../../utils/config');

Page({
  data: {
    userInfo: {},
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().init();
    }
    this.refreshUser();
  },

  // 刷新用户信息：先读缓存，再从服务端刷新
  refreshUser() {
    const stored = wx.getStorageSync('userInfo');
    if (stored) {
      this.applyUser(stored);
    }
    get('/api/user/info')
      .then((data) => {
        const user = data || {};
        wx.setStorageSync('userInfo', user);
        this.applyUser(user);
      })
      .catch(() => {});
  },

  applyUser(user) {
    this.setData({
      userInfo: Object.assign({}, user, {
        avatarUrl: imgUrl((user && user.avatar) || ''),
      }),
    });
  },

  goMyGoods() {
    wx.navigateTo({ url: '/pages/mylist/index?type=my-goods' });
  },

  goFavorite() {
    wx.navigateTo({ url: '/pages/mylist/index?type=favorite' });
  },
});