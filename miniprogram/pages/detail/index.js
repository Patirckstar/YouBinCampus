const { get, post, del, put } = require('../../utils/request');
const { imgUrl } = require('../../utils/config');

// 解析 createTime（形如 2026-10-09T22:10:59）为毫秒时间戳。
function parseTime(t) {
  if (!t) {
    return 0;
  }
  const seg = String(t).split('T');
  const d = seg[0].split('-');
  const tm = (seg[1] || '00:00:00').split(':');
  const date = new Date(
    Number(d[0]),
    Number(d[1]) - 1,
    Number(d[2]),
    Number(tm[0]) || 0,
    Number(tm[1]) || 0,
    Number(tm[2]) || 0
  );
  return date.getTime() || 0;
}

function formatTime(t) {
  const ts = parseTime(t);
  if (!ts) {
    return '';
  }
  const date = new Date(ts);
  const m = date.getMonth() + 1;
  const dd = date.getDate();
  const hh = date.getHours();
  const mm = date.getMinutes();
  return (
    date.getFullYear() +
    '-' +
    (m < 10 ? '0' + m : m) +
    '-' +
    (dd < 10 ? '0' + dd : dd) +
    ' ' +
    (hh < 10 ? '0' + hh : hh) +
    ':' +
    (mm < 10 ? '0' + mm : mm)
  );
}

Page({
  data: {
    id: '',
    goods: null,
    images: [],
    avatarUrl: '',
    timeText: '',
    isFav: false,
    isOwner: false,
    loading: true,
  },

  onLoad(options) {
    const id = (options && options.id) || '';
    this.setData({ id });
    this.loadDetail(id);
    this.checkFavorite(id);
  },

  loadDetail(id) {
    get('/api/goods/' + id)
      .then((goods) => {
        const images = (goods.images || []).map((p) => imgUrl(p));
        const userId = this.currentUserId();
        this.setData({
          goods,
          images,
          avatarUrl: imgUrl(goods.sellerAvatar || ''),
          timeText: formatTime(goods.createTime),
          isOwner: String(goods.userId) === String(userId),
          loading: false,
        });
      })
      .catch(() => {
        this.setData({ loading: false });
      });
  },

  currentUserId() {
    const user = wx.getStorageSync('userInfo');
    return user ? user.id : undefined;
  },

  checkFavorite(id) {
    get('/api/favorite/check/' + id)
      .then((isFav) => {
        this.setData({ isFav: !!isFav });
      })
      .catch(() => {});
  },

  // 联系卖家
  onContact() {
    const goods = this.data.goods;
    if (!goods || !goods.phone) {
      wx.showToast({ title: '卖家未留电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: goods.phone });
  },

  // 收藏 / 取消收藏
  onToggleFav() {
    const id = this.data.id;
    const isFav = this.data.isFav;
    const req = isFav ? del('/api/favorite/' + id) : post('/api/favorite/' + id);
    req
      .then(() => {
        this.setData({ isFav: !isFav });
        wx.showToast({ title: isFav ? '已取消收藏' : '已收藏', icon: 'success' });
      })
      .catch(() => {});
  },

  // 编辑
  onEdit() {
    wx.navigateTo({ url: '/pages/publish/index?id=' + this.data.id });
  },

  // 标记已售
  onSold() {
    const id = this.data.id;
    wx.showModal({
      title: '提示',
      content: '确认将该商品标记为已售？',
      success: (res) => {
        if (res.confirm) {
          put('/api/goods/' + id + '/sold')
            .then(() => {
              wx.showToast({ title: '已标记已售', icon: 'success' });
              this.loadDetail(id);
            })
            .catch(() => {});
        }
      },
    });
  },

  // 删除
  onDelete() {
    wx.showModal({
      title: '提示',
      content: '确认删除该商品？',
      success: (res) => {
        if (res.confirm) {
          del('/api/goods/' + this.data.id)
            .then(() => {
              wx.showToast({ title: '已删除', icon: 'success' });
              setTimeout(() => {
                wx.navigateBack();
              }, 1200);
            })
            .catch(() => {});
        }
      },
    });
  },
});