const { get, put, del } = require('../../utils/request');

Page({
  data: {
    type: 'my-goods', // my-goods 我发布的 / favorite 我收藏的 / want 我想要的
    list: [],
    loading: false,
  },

  onLoad(options) {
    const type = (options && options.type) || 'my-goods';
    this.setData({ type });
    let title = '我发布的';
    if (type === 'favorite') {
      title = '我收藏的';
    } else if (type === 'want') {
      title = '我想要的';
    } else if (type === 'bought') {
      title = '我买到的';
    }
    wx.setNavigationBarTitle({ title });
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
      let req;
      if (this.data.type === 'my-goods') {
        req = get('/api/user/my-goods');
      } else if (this.data.type === 'want') {
        req = get('/api/want/mine');
      } else if (this.data.type === 'bought') {
        req = get('/api/order/mine');
      } else {
        req = get('/api/favorite/list');
      }
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
        if (!res.confirm) {
          return;
        }
        get('/api/want/goods/' + id)
          .then((list) => {
            this.pickBuyer(id, list || []);
          })
          .catch(() => {
            this.doSold(id, '');
          });
      },
    });
  },

  // 选择买家：有想要人则弹 actionSheet，否则/取消则直接不指定买家成交
  pickBuyer(id, list) {
    if (!list.length) {
      this.doSold(id, '');
      return;
    }
    const shown = list.slice(0, 5);
    const itemList = shown.map((u) => (u && u.nickname) || '校园用户');
    itemList.push('直接成交（不指定买家）');
    wx.showActionSheet({
      itemList,
      success: (res) => {
        const idx = res.tapIndex;
        if (idx >= 0 && idx < shown.length) {
          this.doSold(id, shown[idx].userId);
        } else {
          this.doSold(id, '');
        }
      },
      fail: () => {
        this.doSold(id, '');
      },
    });
  },

  doSold(id, buyerId) {
    const url = '/api/goods/' + id + '/sold' + (buyerId ? '?buyerId=' + buyerId : '');
    put(url)
      .then(() => {
        wx.showToast({ title: '已标记已售', icon: 'success' });
        this.loadList();
      })
      .catch(() => { });
  },

  // 下架
  onOffline(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确认下架该商品？',
      success: (res) => {
        if (res.confirm) {
          put('/api/goods/' + id + '/offline')
            .then(() => {
              wx.showToast({ title: '已下架', icon: 'success' });
              this.loadList();
            })
            .catch(() => { });
        }
      },
    });
  },

  // 上架
  onOnline(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确认上架该商品？',
      success: (res) => {
        if (res.confirm) {
          put('/api/goods/' + id + '/online')
            .then(() => {
              wx.showToast({ title: '已上架', icon: 'success' });
              this.loadList();
            })
            .catch(() => { });
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
            .catch(() => { });
        }
      },
    });
  },
});