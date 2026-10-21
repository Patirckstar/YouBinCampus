const { put, upload } = require('../../utils/request');
const { imgUrl } = require('../../utils/config');

Page({
  data: {
    nickname: '',
    phone: '',
    avatar: '', // 相对路径
    avatarUrl: '', // 预览完整地址
    saving: false,
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '编辑资料' });
    const user = wx.getStorageSync('userInfo') || {};
    const avatar = user.avatar || '';
    this.setData({
      nickname: user.nickname || '',
      phone: user.phone || '',
      avatar,
      avatarUrl: imgUrl(avatar),
    });
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value });
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  // 选择并上传头像
  onAvatarTap() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const files = res.tempFiles || [];
        const file = files[0];
        if (!file || !file.tempFilePath) {
          return;
        }
        wx.showLoading({ title: '上传中', mask: true });
        upload(file.tempFilePath)
          .then((path) => {
            wx.hideLoading();
            this.setData({ avatar: path, avatarUrl: imgUrl(path) });
          })
          .catch(() => {
            wx.hideLoading();
          });
      },
    });
  },

  onSave() {
    if (this.data.saving) {
      return;
    }
    const nickname = (this.data.nickname || '').trim();
    const phone = (this.data.phone || '').trim();
    const avatar = this.data.avatar || '';
    if (!nickname && !phone && !avatar) {
      wx.showToast({ title: '请填写内容', icon: 'none' });
      return;
    }

    this.setData({ saving: true });
    put('/api/user/info', { nickname, phone, avatar })
      .then((data) => {
        const user = data || {};
        wx.setStorageSync('userInfo', user);
        wx.showToast({ title: '已保存', icon: 'success' });
        setTimeout(() => {
          wx.navigateBack();
        }, 1200);
      })
      .catch(() => {
        this.setData({ saving: false });
      });
  },
});