const { get, post, put, upload } = require('../../utils/request');
const { imgUrl } = require('../../utils/config');

Page({
  data: {
    id: '', // 编辑时的商品 id
    categories: [],
    categoryNames: [],
    categoryIndex: -1,
    form: {
      title: '',
      description: '',
      categoryId: '',
      price: '',
    },
    images: [], // 相对路径数组
    imageUrls: [], // 完整地址，用于展示
    submitting: false,
    showAgent: false,
    agentText: '',
  },

  onLoad(options) {
    this.setData({ id: (options && options.id) || '' });
    this.loadCategories(() => {
      if (this.data.id) {
        this.loadDetail(this.data.id);
      }
    });
  },

  // 加载分类
  loadCategories(cb) {
    get('/api/category/list')
      .then((list) => {
        const categories = list || [];
        this.setData({
          categories,
          categoryNames: categories.map((c) => c.name),
        });
        if (cb) cb();
      })
      .catch(() => {
        if (cb) cb();
      });
  },

  // 编辑态：加载旧数据填充表单
  loadDetail(id) {
    get('/api/goods/' + id)
      .then((goods) => {
        const images = goods.images || [];
        let categoryIndex = -1;
        if (goods.categoryId !== undefined && goods.categoryId !== null) {
          const idx = this.data.categories.findIndex((c) => String(c.id) === String(goods.categoryId));
          categoryIndex = idx >= 0 ? idx : -1;
        }
        this.setData({
          images,
          imageUrls: images.map((p) => imgUrl(p)),
          categoryIndex,
          form: {
            title: goods.title || '',
            description: goods.description || '',
            categoryId: goods.categoryId === undefined || goods.categoryId === null ? '' : goods.categoryId,
            price: goods.price === undefined || goods.price === null ? '' : String(goods.price),
          },
        });
      })
      .catch(() => {});
  },

  onTitleInput(e) {
    this.setData({ 'form.title': e.detail.value });
  },

  onDescInput(e) {
    this.setData({ 'form.description': e.detail.value });
  },

  onPriceInput(e) {
    this.setData({ 'form.price': e.detail.value });
  },

  onCategoryChange(e) {
    const idx = Number(e.detail.value);
    const cat = this.data.categories[idx];
    this.setData({
      categoryIndex: idx,
      'form.categoryId': cat ? cat.id : '',
    });
  },

  // 选择图片
  onChooseImage() {
    const remain = 9 - this.data.images.length;
    wx.chooseMedia({
      count: remain,
      mediaType: ['image'],
      success: (res) => {
        const files = (res.tempFiles || []).filter((f) => f.tempFilePath);
        this.uploadImages(files);
      },
    });
  },

  // 逐张上传并收集相对路径
  uploadImages(files) {
    if (!files.length) {
      return;
    }
    wx.showLoading({ title: '上传中', mask: true });
    const tasks = files.map((f) => upload(f.tempFilePath));
    Promise.all(tasks)
      .then((paths) => {
        const images = this.data.images.concat(paths);
        this.setData({
          images,
          imageUrls: images.map((p) => imgUrl(p)),
        });
        wx.hideLoading();
      })
      .catch(() => {
        wx.hideLoading();
      });
  },

  // 删除图片
  onRemoveImage(e) {
    const idx = Number(e.currentTarget.dataset.index);
    const images = this.data.images.slice();
    images.splice(idx, 1);
    this.setData({
      images,
      imageUrls: images.map((p) => imgUrl(p)),
    });
  },

  // 智能发布弹层
  onAgentTap() {
    this.setData({ showAgent: true, agentText: '' });
  },

  onAgentInput(e) {
    this.setData({ agentText: e.detail.value });
  },

  onCancelAgent() {
    this.setData({ showAgent: false });
  },

  onConfirmAgent() {
    const text = (this.data.agentText || '').trim();
    if (!text) {
      wx.showToast({ title: '请输入商品描述', icon: 'none' });
      return;
    }
    post('/api/agent/generate-goods', { text })
      .then((res) => {
        const categoryId = res && res.categoryId;
        let categoryIndex = -1;
        if (categoryId !== undefined && categoryId !== null) {
          const idx = this.data.categories.findIndex((c) => String(c.id) === String(categoryId));
          categoryIndex = idx >= 0 ? idx : -1;
        }
        this.setData({
          showAgent: false,
          categoryIndex,
          form: {
            title: (res && res.title) || '',
            description: (res && res.description) || '',
            categoryId: categoryId === undefined || categoryId === null ? '' : categoryId,
            price: res && res.price !== undefined && res.price !== null ? String(res.price) : '',
          },
        });
        wx.showToast({ title: '已生成', icon: 'success' });
      })
      .catch(() => {});
  },

  // 提交表单
  onSubmit() {
    if (this.data.submitting) {
      return;
    }
    const { title, description, categoryId, price } = this.data.form;
    const trimmedTitle = (title || '').trim();
    if (!trimmedTitle) {
      wx.showToast({ title: '请输入标题', icon: 'none' });
      return;
    }
    if (categoryId === '' || categoryId === undefined || categoryId === null) {
      wx.showToast({ title: '请选择分类', icon: 'none' });
      return;
    }
    if (price === '' || price === undefined || price === null || Number(price) < 0) {
      wx.showToast({ title: '请输入价格', icon: 'none' });
      return;
    }

    const payload = {
      categoryId,
      title: trimmedTitle,
      description: description || '',
      price: Number(price),
      images: this.data.images,
    };

    this.setData({ submitting: true });
    const req = this.data.id ? put('/api/goods/' + this.data.id, payload) : post('/api/goods', payload);
    req
      .then(() => {
        wx.showToast({ title: '提交成功', icon: 'success' });
        setTimeout(() => {
          wx.navigateBack();
        }, 1200);
      })
      .catch(() => {
        this.setData({ submitting: false });
      });
  },

  noop() {},
});