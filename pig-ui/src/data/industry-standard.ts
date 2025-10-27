// 行业标准静态数据
export interface IndustryStandard {
  id: number
  standardNumber: string
  standardName: string
  industryField: string
  status: 'current' | 'abolished'
  statusText: string
  approvalDate: string
  implementationDate: string
  pk?: string
}

export interface StandardDetail {
  id: number
  standardNumber: string
  standardName: string
  industryField: string
  status: 'current' | 'abolished'
  statusText: string
  approvalDate: string
  implementationDate: string
  publishDate: string
  implementDate: string
  revisionType: string
  chinaClassification: string
  internationalClassification: string
  technicalCommittee: string
  approvalDepartment: string
  industryClassification: string
  standardCategory: string
  recordNumber: string
  recordDate: string
  scope: string
  draftingUnits: string[]
  draftingPersons: string[]
}

// 示例标准数据
export const sampleStandards: IndustryStandard[] = [
  {
    id: 1,
    standardNumber: 'GY/T 425-2025',
    standardName: '广播级超高清摄像机技术要求和测量方法',
    industryField: '广播电视和网络视听',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-10-11',
    implementationDate: '2025-10-11',
    pk: '0853c578230be85583ca08897687f1aa7ea2cfc1761d93571be4caff0ed180a5'
  },
  {
    id: 2,
    standardNumber: 'HG/T 6384-2025',
    standardName: '聚乙烯醇熟化机',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg638420250819001'
  },
  {
    id: 3,
    standardNumber: 'HG/T 6383-2025',
    standardName: '聚丙烯脱挥塔',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg638320250819001'
  },
  {
    id: 4,
    standardNumber: 'HG/T 6382-2025',
    standardName: '轮胎辐照加工用电子加速器技术要求',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg638220250819001'
  },
  {
    id: 5,
    standardNumber: 'HG/T 6381-2025',
    standardName: '帘线干热收缩测试仪',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg638120250819001'
  },
  {
    id: 6,
    standardNumber: 'HG/T 6380-2025',
    standardName: '试验用橡胶平板硫化机',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg638020250819001'
  },
  {
    id: 7,
    standardNumber: 'HG/T 6379-2025',
    standardName: '转鼓式轮胎滚动阻力试验机',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg637920250819001'
  },
  {
    id: 8,
    standardNumber: 'HG/T 6401-2025',
    standardName: '钛白粉生产副产硫酸',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg640120250819001'
  },
  {
    id: 9,
    standardNumber: 'HG/T 6431-2025',
    standardName: '工业循环冷却水污垢和腐蚀产物中多元素含量测定方法',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg643120250819001'
  },
  {
    id: 10,
    standardNumber: 'HG/T 6400-2025',
    standardName: '水处理剂稳锌性能的测定锌盐沉积法',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg640020250819001'
  },
  {
    id: 11,
    standardNumber: 'HG/T 6399-2025',
    standardName: '高温水系统阻垢剂阻垢性能测定方法',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg639920250819001'
  },
  {
    id: 12,
    standardNumber: 'HG/T 6398-2025',
    standardName: '多元胺锅炉水处理药剂性能测定方法',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg639820250819001'
  },
  {
    id: 13,
    standardNumber: 'HG/T 6388-2025',
    standardName: '光引发剂产品中有机溶剂残留量的测定顶空气相色谱法',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg638820250819001'
  },
  {
    id: 14,
    standardNumber: 'HG/T 6428-2025',
    standardName: '紫外线吸收剂2-(2\'-羟基-5\'-叔辛基苯基)苯并三唑',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg642820250819001'
  },
  {
    id: 15,
    standardNumber: 'HG/T 6427-2025',
    standardName: '硫化促进剂二异丙基黄原四硫醚(DIPT)',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    pk: 'hg642720250819001'
  }
]

// 详细标准信息
export const sampleStandardDetails: Record<number, StandardDetail> = {
  1: {
    id: 1,
    standardNumber: 'GY/T 425-2025',
    standardName: '广播级超高清摄像机技术要求和测量方法',
    industryField: '广播电视和网络视听',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-10-11',
    implementationDate: '2025-10-11',
    publishDate: '2025-10-11',
    implementDate: '2025-10-11',
    revisionType: '制定',
    chinaClassification: 'M70',
    internationalClassification: '33.160.01',
    technicalCommittee: '全国广播电视和网络视听标准化技术委员会SAC/TC 239',
    approvalDepartment: '国家广播电视总局',
    industryClassification: '文化、体育和娱乐业',
    standardCategory: '方法标准',
    recordNumber: '102497-2025',
    recordDate: '2025-10-24',
    scope: '本文件规定了广播级超高清摄像机的基本功能、光电性能、输出信号、音频指标等技术要求和测量方法。本文件适用于广播级超高清摄像机的设计、生产、评测、验收和维护。',
    draftingUnits: [
      '国家广播电视总局广播电视科学研究院',
      '中央广播电视总台',
      '中国传媒大学',
      '佳能(中国)有限公司',
      '索尼(中国)有限公司',
      '松下电器(中国)有限公司',
      '海康威视数字技术股份有限公司',
      '大疆创新科技有限公司',
      '中兴通讯股份有限公司',
      '华为技术有限公司',
      '北京理工大学',
      '电子科技大学'
    ],
    draftingPersons: [
      '石亮', '蔺飞', '欧臻彦', '宁金辉', '张强', '李华', '王明', '陈伟', '赵静', '刘波',
      '孙丽', '周杰', '吴刚', '郑楠', '林涛', '徐军', '杨洋', '何磊', '秦峰', '黄涛',
      '胡伟', '唐亮', '袁文', '易建华', '郭佳', '韩雪', '阎海', '易斌', '甘霖', '霍豫',
      '王自晖', '王昊'
    ]
  },
  2: {
    id: 2,
    standardNumber: 'HG/T 6384-2025',
    standardName: '聚乙烯醇熟化机',
    industryField: '化工',
    status: 'current',
    statusText: '现行',
    approvalDate: '2025-08-19',
    implementationDate: '2026-03-01',
    publishDate: '2025-08-19',
    implementDate: '2026-03-01',
    revisionType: '制定',
    chinaClassification: 'G95',
    internationalClassification: '71.120.10',
    technicalCommittee: '全国橡胶塑料机械标准化技术委员会',
    approvalDepartment: '工业和信息化部',
    industryClassification: '制造业',
    standardCategory: '产品标准',
    recordNumber: '102498-2025',
    recordDate: '2025-08-20',
    scope: '本标准规定了聚乙烯醇熟化机的术语和定义、技术要求、试验方法、检验规则、标志、包装、运输和贮存。本标准适用于聚乙烯醇熟化机的设计、制造和检验。',
    draftingUnits: [
      '中国化工装备协会',
      '大连橡胶塑料机械股份有限公司',
      '青岛科技大学',
      '北京化工大学'
    ],
    draftingPersons: [
      '李明', '王强', '张华', '刘伟', '陈军', '赵磊'
    ]
  }
}

// 部委数据
export const departments = [
  { code: 'ndrc', name: '国家发展和改革委员会', count: 128 },
  { code: 'edu', name: '教育部', count: 317 },
  { code: 'miit', name: '工业和信息化部', count: 41496 },
  { code: 'mps', name: '公安部', count: 2963 },
  { code: 'mca', name: '民政部', count: 226 },
  { code: 'moj', name: '司法部', count: 174 },
  { code: 'mohrss', name: '人力资源和社会保障部', count: 218 },
  { code: 'mnr', name: '自然资源部', count: 1188 },
  { code: 'mee', name: '生态环境部', count: 1396 },
  { code: 'mohurd', name: '住房和城乡建设部', count: 1124 },
  { code: 'mot', name: '交通运输部', count: 1385 },
  { code: 'mwr', name: '水利部', count: 793 },
  { code: 'mara', name: '农业农村部', count: 5613 },
  { code: 'mofcom', name: '商务部', count: 780 },
  { code: 'mct', name: '文化和旅游部', count: 213 },
  { code: 'nhc', name: '卫生健康委员会', count: 994 },
  { code: 'mem', name: '应急管理部', count: 435 },
  { code: 'pbc', name: '中国人民银行', count: 392 },
  { code: 'gac', name: '海关总署', count: 5797 },
  { code: 'sta', name: '国家税务总局', count: 1 },
  { code: 'samr', name: '国家市场监督管理总局', count: 2 },
  { code: 'nrta', name: '国家广播电视总局', count: 310 },
  { code: 'gasp', name: '国家体育总局', count: 70 },
  { code: 'ngoa', name: '国家机关事务管理局', count: 1 },
  { code: 'cma', name: '中国气象局', count: 767 },
  { code: 'nfsra', name: '国家粮食和物资储备局', count: 308 },
  { code: 'nea', name: '国家能源局', count: 9851 },
  { code: 'sastind', name: '国防科工局', count: 1005 },
  { code: 'stma', name: '国家烟草专卖局', count: 603 },
  { code: 'nfga', name: '国家林业和草原局', count: 2205 },
  { code: 'nra', name: '国家铁路局', count: 869 },
  { code: 'caac', name: '中国民用航空局', count: 268 },
  { code: 'spb', name: '国家邮政局', count: 228 },
  { code: 'ncha', name: '国家文物局', count: 124 },
  { code: 'natcm', name: '国家中医药局', count: 10 },
  { code: 'nmsa', name: '国家矿山安全监察局', count: 1358 },
  { code: 'nmpa', name: '国家药监局', count: 1849 },
  { code: 'cfa', name: '国家电影局', count: 49 },
  { code: 'naa', name: '国家档案局', count: 108 },
  { code: 'nca', name: '国家密码管理局', count: 161 },
  { code: 'cnca', name: '国家认证认可监督管理委员会', count: 265 },
  { code: 'nppa', name: '国家新闻出版署', count: 327 },
  { code: 'cea', name: '中国地震局', count: 134 },
  { code: 'acfsmc', name: '中华全国供销合作总社', count: 411 },
  { code: 'nfra', name: '国家消防救援局', count: 192 }
]

// 行业代码数据
export const industries = [
  { code: 'AQ', name: '安全生产', count: 387 },
  { code: 'BB', name: '包装', count: 89 },
  { code: 'CB', name: '船舶', count: 1532 },
  { code: 'CH', name: '测绘', count: 198 },
  { code: 'CJ', name: '城镇建设', count: 527 },
  { code: 'CY', name: '新闻出版', count: 327 },
  { code: 'DA', name: '档案', count: 108 },
  { code: 'DB', name: '地震', count: 134 },
  { code: 'DL', name: '电力', count: 3297 },
  { code: 'DY', name: '电影', count: 15 },
  { code: 'DZ', name: '地质矿产', count: 699 },
  { code: 'EJ', name: '核工业', count: 835 },
  { code: 'FZ', name: '纺织', count: 2085 },
  { code: 'GA', name: '公共安全', count: 2963 },
  { code: 'GC', name: '国家物资储备', count: 3 },
  { code: 'GF', name: '国防工业', count: 0 },
  { code: 'GH', name: '供销合作', count: 411 },
  { code: 'GM', name: '国密', count: 161 },
  { code: 'GY', name: '广播电视和网络视听', count: 310 },
  { code: 'HB', name: '航空', count: 581 },
  { code: 'HG', name: '化工', count: 4320 },
  { code: 'HJ', name: '环境保护', count: 1396 },
  { code: 'HS', name: '海关', count: 87 },
  { code: 'HY', name: '海洋', count: 202 },
  { code: 'JB', name: '机械', count: 13298 },
  { code: 'JC', name: '建材', count: 1563 },
  { code: 'JG', name: '建筑工程', count: 597 },
  { code: 'JR', name: '金融', count: 392 },
  { code: 'JS', name: '机关事务', count: 1 },
  { code: 'JT', name: '交通', count: 1385 },
  { code: 'JY', name: '教育', count: 317 },
  { code: 'KA', name: '矿山安全', count: 26 },
  { code: 'LB', name: '旅游', count: 97 },
  { code: 'LD', name: '劳动和劳动安全', count: 218 },
  { code: 'LS', name: '粮食', count: 305 },
  { code: 'LY', name: '林业', count: 2205 },
  { code: 'MH', name: '民用航空', count: 268 },
  { code: 'MR', name: '市场监管', count: 2 },
  { code: 'MT', name: '煤炭', count: 1348 },
  { code: 'MZ', name: '民政', count: 258 },
  { code: 'NB', name: '能源', count: 3914 },
  { code: 'NY', name: '农业', count: 4774 },
  { code: 'QB', name: '轻工', count: 4202 },
  { code: 'QC', name: '汽车', count: 779 },
  { code: 'QJ', name: '航天', count: 0 },
  { code: 'QX', name: '气象', count: 767 },
  { code: 'RB', name: '认证认可', count: 265 },
  { code: 'RF', name: '人民防空', count: 1 },
  { code: 'SB', name: '国内贸易', count: 958 },
  { code: 'SC', name: '水产', count: 839 },
  { code: 'SF', name: '司法', count: 174 },
  { code: 'SH', name: '石油化工', count: 965 },
  { code: 'SJ', name: '电子', count: 1809 },
  { code: 'SL', name: '水利', count: 793 },
  { code: 'SN', name: '出入境检验检疫', count: 5797 },
  { code: 'SW', name: '税务', count: 1 },
  { code: 'SY', name: '石油天然气', count: 2643 },
  { code: 'TB', name: '铁路', count: 869 },
  { code: 'TD', name: '土地管理', count: 89 },
  { code: 'TY', name: '体育', count: 70 },
  { code: 'WB', name: '物资管理', count: 127 },
  { code: 'WH', name: '文化', count: 116 },
  { code: 'WJ', name: '兵工民品', count: 185 },
  { code: 'WM', name: '外经贸', count: 19 },
  { code: 'WS', name: '卫生', count: 994 },
  { code: 'WW', name: '文物保护', count: 124 },
  { code: 'XB', name: '稀土', count: 177 },
  { code: 'XF', name: '消防救援', count: 192 },
  { code: 'YB', name: '黑色冶金', count: 1765 },
  { code: 'YD', name: '通信', count: 5476 },
  { code: 'YG', name: '烟草', count: 603 },
  { code: 'YJ', name: '减灾救灾与综合', count: 435 },
  { code: 'YS', name: '有色金属', count: 2643 },
  { code: 'YY', name: '医药', count: 1849 },
  { code: 'YZ', name: '邮政', count: 228 },
  { code: 'ZY', name: '中医药', count: 10 }
]

// 字母导航
export const alphabet = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'L', 'M', 'N', 'Q', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z']

// 工具函数
export const getStandardById = (id: number): IndustryStandard | undefined => {
  return sampleStandards.find(standard => standard.id === id)
}

export const getStandardDetailById = (id: number): StandardDetail | undefined => {
  return sampleStandardDetails[id]
}

export const searchStandards = (keyword: string, filters: any = {}): IndustryStandard[] => {
  let results = [...sampleStandards]
  
  // 关键词搜索
  if (keyword) {
    results = results.filter(standard => 
      standard.standardNumber.toLowerCase().includes(keyword.toLowerCase()) ||
      standard.standardName.toLowerCase().includes(keyword.toLowerCase())
    )
  }
  
  // 部委筛选
  if (filters.department) {
    // 这里可以根据实际业务逻辑进行筛选
  }
  
  // 行业筛选
  if (filters.industry) {
    results = results.filter(standard => 
      standard.industryField.includes(filters.industry)
    )
  }
  
  // 状态筛选
  if (filters.status) {
    results = results.filter(standard => standard.status === filters.status)
  }
  
  return results
}
