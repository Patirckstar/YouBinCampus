const { get } = require('../../utils/request');

Page({
  data: {
    categories: [],
    activeIndex: 0,
    goodsList: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    finished: false,
  },

  onLoad() {
    this.loadCategories();
  },

  onPullDownRefresh() {
    this.loadGoods(true).then(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 分类列表
  loadCategories() {
    get('/api/category/list')
      .then((list) => {
        const categories = list || [];
        this.setData({ categories });
        this.loadGoods(true);
      })
      .catch(() => {});
  },

  // 点击左侧分类
  onCategoryTap(e) {
    const idx = Number(e.currentTarget.dataset.index);
    if (idx === this.data.activeIndex) {
      return;
    }
    this.setData({ activeIndex: idx, goodsList: [], pageNum: 1, finished: false });
    this.loadGoods(true);
  },

  // 加载选中分类下的商品
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
      const cat = this.data.categories[this.data.activeIndex];
      if (!cat) {
        resolve();
        return;
      }
      const pageNum = reset ? 1 : this.data.pageNum;
      this.setData({ loading: true });
      get('/api/goods/page', {
        pageNum,
        pageSize: this.data.pageSize,
        categoryId: cat.id,
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

  // 滚动到底加载更多
  onReachBottom() {
    this.loadGoods(false);
  },

  onGoodsTap(e) {
    const goods = e.detail.goods;
    if (goods && goods.id !== undefined) {
      wx.navigateTo({ url: '/pages/detail/index?id=' + goods.id });
    }
  },
});