// 商品/分类 mock 数据，字段与后端 zg-item 服务对齐（price 单位为分）
// 商品图存放于 public/images/，接后端后 image 字段换为 OSS/图床完整 URL 即可
export const categories = [
  { id: 1, name: '手机数码', emoji: '📱' },
  { id: 2, name: '电脑办公', emoji: '💻' },
  { id: 3, name: '家用电器', emoji: '🔌' },
  { id: 4, name: '服饰鞋包', emoji: '👕' },
  { id: 5, name: '食品生鲜', emoji: '🍎' },
  { id: 6, name: '运动户外', emoji: '⚽' }
]

export const items = [
  {
    id: 1001, name: '小米14 Pro 5G手机 骁龙8Gen3 徕卡光学镜头 16GB+512GB 黑色',
    price: 499900, stock: 128, image: '/images/p1001.jpg',
    categoryId: 1, categoryName: '手机数码', brand: '小米',
    spec: '16GB+512GB / 骁龙8Gen3 / 6.73英寸2K屏', sales: 2341,
    desc: '第二代骁龙8移动平台，徕卡可变光圈主摄，2K全等深微曲屏，120W秒充。'
  },
  {
    id: 1002, name: 'Apple iPhone 15 128GB 蓝色 支持移动联通电信5G 双卡双待',
    price: 529900, stock: 86, image: '/images/p1002.webp',
    categoryId: 1, categoryName: '手机数码', brand: '苹果',
    spec: '128GB / A16芯片 / 6.1英寸超视网膜屏', sales: 8902,
    desc: 'A16仿生芯片，4800万像素主摄，灵动岛设计，USB-C接口。'
  },
  {
    id: 1003, name: '华为MatePad 11英寸平板电脑 8GB+128GB 深空灰 学习娱乐平板',
    price: 199900, stock: 200, image: '/images/p1003.jpg',
    categoryId: 2, categoryName: '电脑办公', brand: '华为',
    spec: '8GB+128GB / 120Hz高刷 / HarmonyOS', sales: 1205,
    desc: '11英寸120Hz全面屏，多屏协同，平行视界，学习办公好帮手。'
  },
  {
    id: 1004, name: '联想拯救者Y7000P 电竞游戏笔记本电脑 RTX4060 2.5K 165Hz',
    price: 749900, stock: 45, image: '/images/p1004.webp',
    categoryId: 2, categoryName: '电脑办公', brand: '联想',
    spec: 'i9-14900HX / 16G / 1T固态 / RTX4060', sales: 678,
    desc: '14代酷睿i9处理器，RTX4060独显，2.5K 165Hz电竞屏，霜刃散热。'
  },
  {
    id: 1005, name: '罗技MX Master 3S 无线蓝牙鼠标 人体工学 静音办公',
    price: 54900, stock: 320, image: '/images/p1005.jpg',
    categoryId: 2, categoryName: '电脑办公', brand: '罗技',
    spec: '8K DPI / 静音按键 / 多设备切换', sales: 3456,
    desc: '8000DPI任意表面追踪，静音微动，电磁滚轮，跨三台设备无缝切换。'
  },
  {
    id: 1006, name: 'Apple AirPods Pro 2 无线蓝牙耳机 主动降噪 运动级防水',
    price: 149900, stock: 150, image: '/images/p1006.webp',
    categoryId: 1, categoryName: '手机数码', brand: '苹果',
    spec: 'H2芯片 / 主动降噪 / USB-C充电盒', sales: 5623,
    desc: 'H2芯片更强主动降噪，自适应通透模式，个性化空间音频。'
  },
  {
    id: 1007, name: '米家空气净化器4 Pro 除甲醛除菌 家用卧室智能净化机',
    price: 109900, stock: 98, image: '/images/p1007.jpg',
    categoryId: 3, categoryName: '家用电器', brand: '米家',
    spec: '500m³/h颗粒物CADR / OLED触控屏', sales: 890,
    desc: '三重滤芯，500大风量，米家APP联动，OLED触控显示屏。'
  },
  {
    id: 1008, name: '美的空调 大1.5匹 新一级能效 变频冷暖 自清洁挂机',
    price: 229900, stock: 60, image: '/images/p1008.jpg',
    categoryId: 3, categoryName: '家用电器', brand: '美的',
    spec: '1.5匹 / 新一级能效 / 35㎡适用', sales: 1567,
    desc: '新一级能效省电，56℃高温自清洁，一晚1度电，静音舒适。'
  },
  {
    id: 1009, name: '海尔滚筒洗衣机 10公斤 大容量 除菌螨 智能投放',
    price: 189900, stock: 72, image: '/images/p1009.jpg',
    categoryId: 3, categoryName: '家用电器', brand: '海尔',
    spec: '10kg / 1.1洗净比 / BLDC变频', sales: 723,
    desc: '10公斤大容量，微蒸汽空气洗除菌螨，智能投放洗涤剂。'
  },
  {
    id: 1010, name: '优衣库男装 摇粒绒外套 拉链休闲夹克 秋冬保暖 25新款',
    price: 19900, stock: 500, image: '/images/p1010.jpg',
    categoryId: 4, categoryName: '服饰鞋包', brand: '优衣库',
    spec: 'S-XL / 多色可选 / 100%聚酯纤维', sales: 4321,
    desc: '蓬松摇粒绒，轻盈保暖不易起静电，多色可选，秋冬百搭。'
  },
  {
    id: 1011, name: 'Nike耐克 Air Zoom Pegasus 40 男子缓震跑步鞋',
    price: 59900, stock: 210, image: '/images/p1011.webp',
    categoryId: 4, categoryName: '服饰鞋包', brand: '耐克',
    spec: '39-45码 / React泡棉 / 飞线鞋面', sales: 2890,
    desc: '前后Zoom Air气垫，React中底泡棉，透气飞线鞋面，日常慢跑首选。'
  },
  {
    id: 1012, name: '三只松鼠每日坚果 30包750g 混合坚果零食礼包',
    price: 6900, stock: 1000, image: '/images/p1012.jpg',
    categoryId: 5, categoryName: '食品生鲜', brand: '三只松鼠',
    spec: '750g/30包 / 6种坚果+3种果干', sales: 12340,
    desc: '科学配比每日一包，6种坚果仁搭配3种果干，独立小包装。'
  },
  {
    id: 1013, name: '大疆 DJI Osmo Pocket 3 一英寸口袋云台相机 全能套装',
    price: 299900, stock: 38, image: '/images/p1013.jpg',
    categoryId: 1, categoryName: '手机数码', brand: '大疆',
    spec: '一英寸CMOS / 2英寸旋转触摸屏 / 4K120fps', sales: 987,
    desc: '一英寸传感器，三轴机械增稳，2英寸旋转触摸屏，Vlog神器。'
  },
  {
    id: 1014, name: '迪卡侬 运动跑步T恤 男速干短袖 透气健身衣服 HUB',
    price: 4990, stock: 800, image: '/images/p1014.webp',
    categoryId: 6, categoryName: '运动户外', brand: '迪卡侬',
    spec: 'S-2XL / 速干面料 / 多色', sales: 6543,
    desc: '速干透气面料，运动剪裁不束缚，跑步健身日常百搭。'
  },
  {
    id: 1015, name: '小米手环9 NFC版 运动健康智能手环 表盘商城',
    price: 24900, stock: 660, image: '/images/p1015.webp',
    categoryId: 1, categoryName: '手机数码', brand: '小米',
    spec: '1.62英寸AMOLED / NFC / 16天续航', sales: 9217,
    desc: '轻薄金属机身，心率血氧监测，NFC门禁公交，16天长续航。'
  },
  {
    id: 1016, name: '京东京造 记忆棉枕头 颈椎护颈助眠枕 慢回弹低枕',
    price: 12900, stock: 430, image: '/images/p1016.jpg',
    categoryId: 5, categoryName: '食品生鲜', brand: '京造',
    spec: '慢回弹记忆棉 / B型曲线 / 可洗枕套', sales: 3210,
    desc: '太空记忆棉慢回弹，人体工学曲线承托颈椎，抗菌可拆洗枕套。'
  }
]

export function formatPrice(priceFen) {
  return (priceFen / 100).toFixed(2)
}
