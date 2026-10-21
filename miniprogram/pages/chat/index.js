const { get, post } = require('../../utils/request');

Page({
  data: {
    goodsId: '',
    peerId: '',
    peerNickname: '',
    goodsTitle: '',
    userId: '',
    messages: [],
    inputValue: '',
  },

  onLoad(options) {
    const goodsId = (options && options.goodsId) || '';
    const peerId = (options && options.peerId) || '';
    // 小程序 navigateTo 的 query 会自动解码，这里直接读取即可。
    const peerNickname = (options && options.peerNickname) || '';
    const goodsTitle = (options && options.goodsTitle) || '';
    const user = wx.getStorageSync('userInfo');
    const userId = user ? user.id : '';
    this.setData({ goodsId, peerId, peerNickname, goodsTitle, userId });
    wx.setNavigationBarTitle({ title: peerNickname || goodsTitle || '聊天' });
  },

  onShow() {
    this.loadMessages();
    this.startPolling();
  },

  onHide() {
    this.stopPolling();
  },

  onUnload() {
    this.stopPolling();
  },

  startPolling() {
    this.stopPolling();
    this.timer = setInterval(() => {
      this.loadMessages(true);
    }, 3000);
  },

  stopPolling() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  },

  loadMessages(silent) {
    const url = '/api/message/with/' + this.data.goodsId + '/' + this.data.peerId;
    get(url)
      .then((list) => {
        const messages = (list || []).map((m) =>
          Object.assign({}, m, {
            isMine: String(m.fromUserId) === String(this.data.userId),
          })
        );
        this.setData({ messages });
        if (!silent) {
          this.scrollToBottom();
        }
      })
      .catch(() => { });
  },

  onInput(e) {
    this.setData({ inputValue: e.detail.value });
  },

  onSend() {
    const content = (this.data.inputValue || '').trim();
    if (!content) {
      return;
    }
    post('/api/message/send', {
      goodsId: this.data.goodsId,
      toUser: this.data.peerId,
      content,
    })
      .then((msg) => {
        const item = Object.assign({}, msg, {
          isMine: String(msg.fromUserId) === String(this.data.userId),
        });
        this.setData({
          messages: this.data.messages.concat([item]),
          inputValue: '',
        });
        this.scrollToBottom();
      })
      .catch(() => { });
  },

  scrollToBottom() {
    wx.pageScrollTo({ scrollTop: 999999, duration: 200 });
  },
});