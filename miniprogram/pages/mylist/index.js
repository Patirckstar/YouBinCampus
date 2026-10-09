const { get, put, del } = require('../../utils/request');

Page({
  data: {
    type: 'my-goods', // my-goods 我发布的 / favorite 我收藏的
    list: [],
    loading: false,
  },

  onLoad(options) {
    const type = (options && options.type) || 'my-goods';
    this.setData({ type });
    wx.setNavigationBarTitle({
      title: type === 'my-goods' ? '我发布的' : '我收藏的',
    });
  },

  onShow() {
    this.loadList();
  },

  onPullDownRefresh() {
    this.loadList().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  loadList() {
    return new Promise((resolve) => {
      this.setData({ loading: true });
      const req = this.data.type === 'my-goods' ? get('/api/user/my-goods') : get('/api/favorite/list');
      req
        .then((list) => {
          this.setData({ list: list || [], loading: false });
          resolve();
        })
        .catch(() => {
          this.setData({ loading: false });
          resolve();
        });
    });
  },

  onGoodsTap(e) {
    const goods = e.detail.goods;
    if (goods && goods.id !== undefined) {
      wx.navigateTo({ url: '/pages/detail/index?id=' + goods.id });
    }
  },

  onEdit(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/publish/index?id=' + id });
  },

  onSold(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确认将该商品标记为已售？',
      success: (res) => {
        if (res.confirm) {
          put('/api/goods/' + id + '/sold')
            .then(() => {
              wx.showToast({ title: '已标记已售', icon: 'success' });
              this.loadList();
            })
            .catch(() => {});
        }
      },
    });
  },

  onDelete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确认删除该商品？',
      success: (res) => {
        if (res.confirm) {
          del('/api/goods/' + id)
            .then(() => {
              wx.showToast({ title: '已删除', icon: 'success' });
              this.loadList();
            })
            .catch(() => {});
        }
      },
    });
  },
});