const { get } = require('../../utils/request');

Page({
  data: {
    categories: [],
    activeIndex: -1, // -1 表示“全部”
    goodsList: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    finished: false,
  },

  onLoad() {
    this.loadCategories();
    this.loadGoods(true);
  },

  onPullDownRefresh() {
    this.loadCategories();
    this.loadGoods(true).then(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    this.loadGoods(false);
  },

  // 加载分类列表
  loadCategories() {
    get('/api/category/list')
      .then((list) => {
        this.setData({ categories: list || [] });
      })
      .catch(() => {});
  },

  // 加载商品列表，reset 为 true 表示重新从第一页开始
  loadGoods(reset) {
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
      const categoryId = this.data.activeIndex === -1 ? '' : (this.data.categories[this.data.activeIndex] || {}).id;
      this.setData({ loading: true });
      get('/api/goods/page', {
        pageNum,
        pageSize: this.data.pageSize,
        categoryId: categoryId === undefined ? '' : categoryId,
      })
        .then((res) => {
          const records = (res && res.records) || [];
          const total = (res && res.total) || 0;
          const newList = reset ? records : this.data.goodsList.concat(records);
          this.setData({
            goodsList: newList,
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

  // 点击分类
  onCategoryTap(e) {
    const idx = e.currentTarget.dataset.index;
    const activeIndex = idx === undefined ? -1 : Number(idx);
    if (activeIndex === this.data.activeIndex) {
      return;
    }
    this.setData({ activeIndex, goodsList: [], pageNum: 1, finished: false });
    this.loadGoods(true);
  },

  // 点击商品卡片，跳详情
  onGoodsTap(e) {
    const goods = e.detail.goods;
    if (goods && goods.id !== undefined) {
      wx.navigateTo({ url: '/pages/detail/index?id=' + goods.id });
    }
  },

  // 跳搜索页
  onSearchTap() {
    wx.navigateTo({ url: '/pages/search/index' });
  },

  // 跳发布页
  onPublish() {
    wx.navigateTo({ url: '/pages/publish/index' });
  },
});