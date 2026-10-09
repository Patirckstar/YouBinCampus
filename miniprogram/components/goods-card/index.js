const { imgUrl } = require('../../utils/config');

// 解析后端返回的 createTime（形如 2026-10-09T22:10:59）为毫秒时间戳。
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

// 相对时间展示。
function formatTime(t) {
  const ts = parseTime(t);
  if (!ts) {
    return '';
  }
  const now = Date.now();
  const diff = now - ts;
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  if (diff < minute) {
    return '刚刚';
  }
  if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前';
  }
  if (diff < day) {
    return Math.floor(diff / hour) + '小时前';
  }
  if (diff < 30 * day) {
    return Math.floor(diff / day) + '天前';
  }
  const date = new Date(ts);
  const m = date.getMonth() + 1;
  const dd = date.getDate();
  return date.getFullYear() + '-' + (m < 10 ? '0' + m : m) + '-' + (dd < 10 ? '0' + dd : dd);
}

function getStatusText(status) {
  if (status === 1) {
    return '在售';
  }
  if (status === 0) {
    return '已下架';
  }
  if (status === 2) {
    return '已售';
  }
  return '';
}

function getStatusClass(status) {
  if (status === 1) {
    return 'status-on';
  }
  if (status === 2) {
    return 'status-sold';
  }
  return 'status-off';
}

Component({
  properties: {
    // 商品对象（GoodsVO）
    goods: {
      type: Object,
      value: {},
    },
    // 是否显示状态标签
    showStatus: {
      type: Boolean,
      value: false,
    },
  },

  data: {
    image: '',
    statusText: '',
    statusClass: '',
    timeText: '',
  },

  observers: {
    goods(goods) {
      if (!goods) {
        return;
      }
      const images = goods.images || [];
      this.setData({
        image: images.length ? imgUrl(images[0]) : '',
        statusText: getStatusText(goods.status),
        statusClass: getStatusClass(goods.status),
        timeText: formatTime(goods.createTime),
      });
    },
  },

  methods: {
    onTap() {
      this.triggerEvent('tap', { goods: this.data.goods });
    },
  },
});