const { get, post } = require('../../utils/request');

Page({
  data: {
    keyword: '',
    searchMode: 'normal', // normal 普通关键词 / ai 自然语言
    list: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    finished: false,
    searched: false,
    history: [],
  },

  onLoad() {
    this.setData({ history: wx.getStorageSync('searchHistory') || [] });
  },

  onReachBottom() {
    if (this.data.searchMode === 'ai') {
      this.doAiSearch(false);
    } else {
      this.doNormalSearch(false);
    }
  },

  onInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  // 普通关键词搜索
  onSearch() {
    const kw = (this.data.keyword || '').trim();
    if (!kw) {
      wx.showToast({ title: '请输入关键词', icon: 'none' });
      return;
    }
    this.saveHistory(kw);
    this.setData({ searchMode: 'normal', searched: true, list: [], pageNum: 1, finished: false });
    this.doNormalSearch(true);
  },

  doNormalSearch(reset) {
    return new Promise((resolve) => {
      if (this.data.loading) {
        resolve();
        return;
      }
      if (!reset && this.data.finished) {
        resolve();
        return;
      }
      const pageNum = reset ? 1 : this.data.pageNum;
      this.setData({ loading: true });
      get('/api/goods/page', {
        pageNum,
        pageSize: this.data.pageSize,
        keyword: (this.data.keyword || '').trim(),
      })
        .then((res) => {
          const records = (res && res.records) || [];
          const total = (res && res.total) || 0;
          const newList = reset ? records : this.data.list.concat(records);
          this.setData({
            list: newList,
            pageNum: pageNum + 1,
            total,
            finished: newList.length >= total,
            loading: false,
          });
          resolve();
        })
        .catch(() => {
          this.setData({ loading: false });
          resolve();
        });
    });
  },

  // AI 自然语言搜索
  onAiSearch() {
    const kw = (this.data.keyword || '').trim();
    if (!kw) {
      wx.showToast({ title: '请输入描述', icon: 'none' });
      return;
    }
    this.saveHistory(kw);
    this.setData({ searchMode: 'ai', searched: true, list: [], pageNum: 1, finished: false });
    this.doAiSearch(true);
  },

  doAiSearch(reset) {
    return new Promise((resolve) => {
      if (this.data.loading) {
        resolve();
        return;
      }
      if (!reset && this.data.finished) {
        resolve();
        return;
      }
      const pageNum = reset ? 1 : this.data.pageNum;
      this.setData({ loading: true });
      post('/api/agent/search', {
        query: (this.data.keyword || '').trim(),
        pageNum,
        pageSize: this.data.pageSize,
      })
        .then((res) => {
          const records = (res && res.records) || [];
          const total = (res && res.total) || 0;
          const newList = reset ? records : this.data.list.concat(records);
          this.setData({
            list: newList,
            pageNum: pageNum + 1,
            total,
            finished: newList.length >= total,
            loading: false,
          });
          resolve();
        })
        .catch(() => {
          this.setData({ loading: false });
          resolve();
        });
    });
  },

  // 保存历史记录（最多 10 条）
  saveHistory(kw) {
    let history = wx.getStorageSync('searchHistory') || [];
    history = history.filter((h) => h !== kw);
    history.unshift(kw);
    history = history.slice(0, 10);
    wx.setStorageSync('searchHistory', history);
    this.setData({ history });
  },

  onHistoryTap(e) {
    const kw = e.currentTarget.dataset.kw;
    this.setData({ keyword: kw, searchMode: 'normal', searched: true, list: [], pageNum: 1, finished: false });
    this.doNormalSearch(true);
  },

  onGoodsTap(e) {
    const goods = e.detail.goods;
    if (goods && goods.id !== undefined) {
      wx.navigateTo({ url: '/pages/detail/index?id=' + goods.id });
    }
  },
});