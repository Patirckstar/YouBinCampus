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
    isWanted: false,
    wantCount: 0,
    loading: true,
  },

  onLoad(options) {
    const id = (options && options.id) || '';
    this.setData({ id });
    this.loadDetail(id);
    this.checkFavorite(id);
    this.checkWant(id);
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
          wantCount: goods.wantCount || 0,
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
      .catch(() => { });
  },

  checkWant(id) {
    get('/api/want/check/' + id)
      .then((isWanted) => {
        this.setData({ isWanted: !!isWanted });
      })
      .catch(() => { });
  },

  // 联系卖家：先取电话，有电话才记录留痕并拨号
  onContact() {
    const id = this.data.id;
    get('/api/goods/' + id + '/contact')
      .then((data) => {
        const phone = data && data.phone;
        if (!phone) {
          wx.showToast({ title: '卖家未留电话', icon: 'none' });
          return;
        }
        // 记录留痕失败不影响拨号
        post('/api/contact/' + id).catch(() => { });
        wx.makePhoneCall({ phoneNumber: phone });
      })
      .catch((err) => {
        if (err && err.code === 403) {
          wx.showToast({ title: '不能联系自己发布的商品', icon: 'none' });
        }
      });
  },

  // 发消息：进入与该商品卖家的聊天（仅买家，非 owner）。
  onMessage() {
    const goods = this.data.goods;
    if (!goods) {
      return;
    }
    const peerNickname = goods.sellerNickname || '';
    const goodsTitle = goods.title || '';
    wx.navigateTo({
      url:
        '/pages/chat/index?goodsId=' +
        this.data.id +
        '&peerId=' +
        goods.userId +
        '&peerNickname=' +
        encodeURIComponent(peerNickname) +
        '&goodsTitle=' +
        encodeURIComponent(goodsTitle),
    });
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
      .catch(() => { });
  },

  // 想要 / 取消想要
  onToggleWant() {
    const id = this.data.id;
    const isWanted = this.data.isWanted;
    const req = isWanted ? del('/api/want/' + id) : post('/api/want/' + id);
    req
      .then(() => {
        this.setData({ isWanted: !isWanted });
        wx.showToast({ title: isWanted ? '已取消想要' : '已想要', icon: 'success' });
        this.loadDetail(id);
      })
      .catch(() => { });
  },

  // 编辑
  onEdit() {
    wx.navigateTo({ url: '/pages/publish/index?id=' + this.data.id });
  },

  // 标记已售（确认后可选买家，再成交）
  onSold() {
    const id = this.data.id;
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
    // showActionSheet 最多 6 项：最多展示前 5 个想要人 + 末尾兜底「直接成交」
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
        // 用户取消 actionSheet，按不指定买家继续成交，避免卡住
        this.doSold(id, '');
      },
    });
  },

  doSold(id, buyerId) {
    const url = '/api/goods/' + id + '/sold' + (buyerId ? '?buyerId=' + buyerId : '');
    put(url)
      .then(() => {
        wx.showToast({ title: '已标记已售', icon: 'success' });
        this.loadDetail(id);
      })
      .catch(() => { });
  },

  // 下架
  onOffline() {
    const id = this.data.id;
    wx.showModal({
      title: '提示',
      content: '确认下架该商品？',
      success: (res) => {
        if (res.confirm) {
          put('/api/goods/' + id + '/offline')
            .then(() => {
              wx.showToast({ title: '已下架', icon: 'success' });
              this.loadDetail(id);
            })
            .catch(() => { });
        }
      },
    });
  },

  // 上架
  onOnline() {
    const id = this.data.id;
    wx.showModal({
      title: '提示',
      content: '确认上架该商品？',
      success: (res) => {
        if (res.confirm) {
          put('/api/goods/' + id + '/online')
            .then(() => {
              wx.showToast({ title: '已上架', icon: 'success' });
              this.loadDetail(id);
            })
            .catch(() => { });
        }
      },
    });
  },

  // 谁想要（仅卖家）
  onShowWanters() {
    const id = this.data.id;
    get('/api/want/goods/' + id)
      .then((list) => {
        const arr = list || [];
        if (!arr.length) {
          wx.showToast({ title: '还没有人想要', icon: 'none' });
          return;
        }
        const names = arr
          .slice(0, 5)
          .map((u) => (u && u.nickname) || '校园用户')
          .join('、');
        const more = arr.length > 5 ? ' 等' + arr.length + '人' : '';
        wx.showModal({
          title: '谁想要(' + arr.length + ')',
          content: names + more,
          showCancel: false,
        });
      })
      .catch(() => { });
  },

  // 联系记录（仅卖家）
  onShowContacts() {
    const id = this.data.id;
    get('/api/contact/goods/' + id)
      .then((list) => {
        const arr = list || [];
        if (!arr.length) {
          wx.showToast({ title: '还没有人联系', icon: 'none' });
          return;
        }
        wx.showModal({
          title: '联系记录',
          content: '共 ' + arr.length + ' 人联系过你',
          showCancel: false,
        });
      })
      .catch(() => { });
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
            .catch(() => { });
        }
      },
    });
  },
});