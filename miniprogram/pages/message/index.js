const { get } = require('../../utils/request');
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

// 会话时间：今天显示 HH:mm，否则 MM-DD。
function formatTime(t) {
  const ts = parseTime(t);
  if (!ts) {
    return '';
  }
  const date = new Date(ts);
  const now = new Date();
  const isToday =
    now.getFullYear() === date.getFullYear() &&
    now.getMonth() === date.getMonth() &&
    now.getDate() === date.getDate();
  const hh = date.getHours();
  const mm = date.getMinutes();
  if (isToday) {
    return (hh < 10 ? '0' + hh : hh) + ':' + (mm < 10 ? '0' + mm : mm);
  }
  const M = date.getMonth() + 1;
  const dd = date.getDate();
  return (M < 10 ? '0' + M : M) + '-' + (dd < 10 ? '0' + dd : dd);
}

Page({
  data: {
    list: [],
    loading: false,
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().init();
    }
    this.loadList();
  },

  loadList() {
    this.setData({ loading: true });
    get('/api/message/conversations')
      .then((list) => {
        const arr = (list || []).map((item) =>
          Object.assign({}, item, {
            peerAvatar: imgUrl((item && item.peerAvatar) || ''),
            goodsImage: imgUrl((item && item.goodsImage) || ''),
            timeText: formatTime(item && item.lastTime),
          })
        );
        // 未读优先，再按最近时间倒序。
        arr.sort((a, b) => {
          const au = a.unreadCount > 0 ? 1 : 0;
          const bu = b.unreadCount > 0 ? 1 : 0;
          if (au !== bu) {
            return bu - au;
          }
          return parseTime(b.lastTime) - parseTime(a.lastTime);
        });
        this.setData({ list: arr, loading: false });
      })
      .catch(() => {
        this.setData({ loading: false });
      });
  },

  onConvTap(e) {
    const item = e.currentTarget.dataset.item;
    if (!item) {
      return;
    }
    const peerNickname = item.peerNickname || '';
    const goodsTitle = item.goodsTitle || '';
    wx.navigateTo({
      url:
        '/pages/chat/index?goodsId=' +
        item.goodsId +
        '&peerId=' +
        item.peerId +
        '&peerNickname=' +
        encodeURIComponent(peerNickname) +
        '&goodsTitle=' +
        encodeURIComponent(goodsTitle),
    });
  },
});