package yokwe.finance.data.fund.jp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.data.provider.jita.StorageJITA;
import yokwe.finance.data.provider.moneybu.StorageMoneybu;
import yokwe.finance.data.provider.nikkei.StorageNikkei;
import yokwe.finance.data.type.DailyValue;
import yokwe.finance.data.type.FundDivInfo;
import yokwe.util.Makefile;
import yokwe.util.UnexpectedException;
import yokwe.util.update.UpdateBase;

public class UpdateFundDiv extends UpdateBase {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	protected static Makefile MAKEFILE = Makefile.builder().
		input(StorageFundJP.FundInfo, StorageJITA.FundDiv, StorageNikkei.FundDivInfo).
		output(StorageFundJP.FundDiv).
		build();

	public static void main(String[] args) {
		callUpdate();
	}

	@Override
	public void update() {
		update_moneybu();
	}
	public void update_FundDivInfo_price() {
//		19:58:23.551 [main] INFO  UpdateFundDiv - fundInfoList  5853
//		19:58:23.635 [main] INFO  UpdateFundDiv - XX  JP3027650005  13210  10.000  ＮＥＸＴ　ＦＵＮＤＳ日経２２５連動型上場投信
//		19:58:23.828 [main] INFO  UpdateFundDiv - XX  JP3046680009  16290  10.000  ＮＥＸＴ　ＦＵＮＤＳ商社・卸売（ＴＯＰＩＸ－１７）上場投信
//		19:58:23.994 [main] INFO  UpdateFundDiv - XX  JP3047240001  15450  10.000  ＮＥＸＴ　ＦＵＮＤＳ　ＮＡＳＤＡＱ－１００（為替ヘッジなし）連動型上場投信
//		19:58:24.002 [main] INFO  UpdateFundDiv - XX  JP3047250000  15460  100.000  ＮＥＸＴ　ＦＵＮＤＳダウ・ジョーンズ工業株３０種平均株価（為替ヘッジなし）連動型上場投信
//		19:58:24.369 [main] INFO  UpdateFundDiv - XX  JP3048390003  14890  100.000  ＮＥＸＴ　ＦＵＮＤＳ日経平均高配当株５０指数連動型上場投信
//		19:58:43.868 [main] INFO  UpdateFundDiv - countA  169
//		19:58:43.869 [main] INFO  UpdateFundDiv - countB  221
//		19:58:43.869 [main] INFO  UpdateFundDiv - countC  2760
//		19:58:43.869 [main] INFO  UpdateFundDiv - countD  2698
//		19:58:43.869 [main] INFO  UpdateFundDiv - countE  5

		var fundDivInfoMap = StorageNikkei.FundDivInfo.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));

		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		int countE = 0;

		var fundInfoList = StorageFundJP.FundInfo.getList();
		logger.info("fundInfoList  {}", fundInfoList.size());

		for(var fundInfo: fundInfoList) {
			var divList     = StorageJITA.FundDiv.getList(fundInfo.isinCode);
			var fundDivInfo = fundDivInfoMap.get(fundInfo.isinCode);

			if (fundDivInfo == null) {
				countA++;
			} else {
				if (divList.isEmpty()) {
					countB++;
				} else if (allZero(divList)) {
					countC++;
				} else {
					var divDate  = fundDivInfo.divDate;
					var divPrice = fundDivInfo.divPrice;

					if (!FundDivInfo.isValid(divDate)) {
						logger.error("Unexpected divDate");
						logger.error("  {}  {}  {}", fundDivInfo.isinCode, fundDivInfo.stockCode, fundDivInfo.name);
						throw new UnexpectedException("Unexpected divDate");
					}
					if (!FundDivInfo.isValid(divPrice)) {
						logger.error("Unexpected divPrice");
						logger.error("  {}  {}  {}", fundDivInfo.isinCode, fundDivInfo.stockCode, fundDivInfo.name);
						throw new UnexpectedException("Unexpected divPrice");
					}

					var priceList = StorageJITA.FundPrice.getList(fundDivInfo.isinCode);
					var myPrice   = priceList.stream().filter(o -> o.date.equals(divDate)).map(o -> o.price).findFirst().orElse(null);
					if (myPrice == null) {
						logger.error("Unexpected divDate");
						logger.error("  {}  {}  {}  {}", fundDivInfo.isinCode, fundDivInfo.stockCode, divDate, fundDivInfo.name);
						throw new UnexpectedException("Unexpected divDate");
					}

					if (divPrice.compareTo(myPrice) == 0) {
						countD++;
					} else {
						countE++;
						// modify divList with factor
						var factor = divPrice.divide(myPrice, 3, RoundingMode.HALF_EVEN);
						logger.info("XX  {}  {}  {}  {}", fundInfo.isinCode, fundInfo.stockCode, factor.toPlainString(), fundInfo.name);
						// modify divList with factor
						for(var e: divList) {
							e.value = e.value.multiply(factor);
						}
					}
				}
			}

			StorageFundJP.FundDiv.save(fundInfo.isinCode, divList);
		}

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
		logger.info("countE  {}", countE);

		StorageFundJP.FundDiv.touch();
	}


	public void update_FundDivInfo_div() {
//		20:04:09.532 [main] INFO  UpdateFundDiv - fundInfoList  5853
//		20:04:09.563 [main] INFO  UpdateFundDiv - XX  JP3027650005  13210  10.000  ＮＥＸＴ　ＦＵＮＤＳ日経２２５連動型上場投信
//		20:04:09.593 [main] INFO  UpdateFundDiv - XX  JP3046680009  16290  10.000  ＮＥＸＴ　ＦＵＮＤＳ商社・卸売（ＴＯＰＩＸ－１７）上場投信
//		20:04:09.612 [main] INFO  UpdateFundDiv - XX  JP3047240001  15450  10.000  ＮＥＸＴ　ＦＵＮＤＳ　ＮＡＳＤＡＱ－１００（為替ヘッジなし）連動型上場投信
//		20:04:09.613 [main] INFO  UpdateFundDiv - XX  JP3047250000  15460  100.000  ＮＥＸＴ　ＦＵＮＤＳダウ・ジョーンズ工業株３０種平均株価（為替ヘッジなし）連動型上場投信
//		20:04:09.672 [main] INFO  UpdateFundDiv - XX  JP3048390003  14890  100.000  ＮＥＸＴ　ＦＵＮＤＳ日経平均高配当株５０指数連動型上場投信
//		20:04:14.090 [main] INFO  UpdateFundDiv - countA  169
//		20:04:14.090 [main] INFO  UpdateFundDiv - countB  221
//		20:04:14.090 [main] INFO  UpdateFundDiv - countC  2760
//		20:04:14.090 [main] INFO  UpdateFundDiv - countD  423
//		20:04:14.090 [main] INFO  UpdateFundDiv - countE  2275
//		20:04:14.090 [main] INFO  UpdateFundDiv - countF  5

		var fundDivInfoMap = StorageNikkei.FundDivInfo.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));

		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		int countE = 0;
		int countF = 0;

		var fundInfoList = StorageFundJP.FundInfo.getList();
		logger.info("fundInfoList  {}", fundInfoList.size());

		for(var fundInfo: fundInfoList) {
			var divList     = StorageJITA.FundDiv.getList(fundInfo.isinCode);
			var fundDivInfo = fundDivInfoMap.get(fundInfo.isinCode);

			if (fundDivInfo == null) {
				countA++;
			} else {
				if (divList.isEmpty()) {
					countB++;
				} else if (allZero(divList)) {
					countC++;
				} else {
					var divDate  = fundDivInfo.divDate;
					var divValue = fundDivInfo.divValue;

					if (!FundDivInfo.isValid(divDate)) {
						logger.error("Unexpected divDate");
						logger.error("  {}  {}  {}", fundDivInfo.isinCode, fundDivInfo.stockCode, fundDivInfo.name);
						throw new UnexpectedException("Unexpected divDate");
					}
					if (!FundDivInfo.isValid(divValue)) {
						logger.error("Unexpected divValue");
						logger.error("  {}  {}  {}", fundDivInfo.isinCode, fundDivInfo.stockCode, fundDivInfo.name);
						throw new UnexpectedException("Unexpected divPrice");
					}

					var myValue = divList.stream().filter(o -> o.date.equals(divDate)).map(o -> o.value).findFirst().orElse(null);
					if (myValue == null) {
						logger.error("Unexpected divDate");
						logger.error("  {}  {}  {}  {}", fundDivInfo.isinCode, fundDivInfo.stockCode, divDate, fundDivInfo.name);
						throw new UnexpectedException("Unexpected divDate");
					}

					if (myValue.compareTo(BigDecimal.ZERO) == 0) {
						countD++;
					} else if (divValue.compareTo(myValue) == 0) {
						countE++;
					} else {
						countF++;
						// modify divList with factor
						var factor = divValue.divide(myValue, 3, RoundingMode.HALF_EVEN);
						logger.info("XX  {}  {}  {}  {}", fundInfo.isinCode, fundInfo.stockCode, factor.toPlainString(), fundInfo.name);
						// modify divList with factor
						for(var e: divList) {
							e.value = e.value.multiply(factor);
						}
					}
				}
			}

			StorageFundJP.FundDiv.save(fundInfo.isinCode, divList);
		}

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
		logger.info("countE  {}", countE);
		logger.info("countF  {}", countF);

		StorageFundJP.FundDiv.touch();
	}


	public void update_moneybu() {
//		21:03:40.149 [main] INFO  UpdateFundDiv - fundInfoList  5853
//		21:03:40.153 [main] INFO  UpdateFundDiv - XX  JP3013190008  13190  0.010  ＮＥＸＴ　ＦＵＮＤＳ日経３００株価指数連動型上場投信
//		21:03:40.163 [main] INFO  UpdateFundDiv - XX  JP3027620008  13050  0.100  ｉＦｒｅｅＥＴＦ　ＴＯＰＩＸ（年１回決算型）
//		21:03:40.165 [main] INFO  UpdateFundDiv - XX  JP3027630007  13060  0.010  ＮＥＸＴ　ＦＵＮＤＳ　ＴＯＰＩＸ連動型上場投信
//		21:03:40.170 [main] INFO  UpdateFundDiv - XX  JP3027650005  13210  0.100  ＮＥＸＴ　ＦＵＮＤＳ日経２２５連動型上場投信
//		21:03:40.174 [main] INFO  UpdateFundDiv - XX  JP3027710007  13290  0.010  ｉシェアーズ・コア日経２２５ＥＴＦ
//		21:03:40.177 [main] INFO  UpdateFundDiv - XX  JP3039100007  13080  0.010  上場インデックスファンドＴＯＰＩＸ
//		21:03:40.179 [main] INFO  UpdateFundDiv - XX  JP3040140000  13110  0.010  ＮＥＸＴ　ＦＵＮＤＳ　ＴＯＰＩＸ　Ｃｏｒｅ３０連動型上場投信
//		21:03:40.181 [main] INFO  UpdateFundDiv - XX  JP3040170007  16150  0.010  ＮＥＸＴ　ＦＵＮＤＳ東証銀行業株価指数連動型上場投信
//		21:03:40.187 [main] INFO  UpdateFundDiv - XX  JP3046560003  16170  0.100  ＮＥＸＴ　ＦＵＮＤＳ食品（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.188 [main] INFO  UpdateFundDiv - XX  JP3046570002  16180  0.100  ＮＥＸＴ　ＦＵＮＤＳエネルギー資源（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.191 [main] INFO  UpdateFundDiv - XX  JP3046580001  16190  0.100  ＮＥＸＴ　ＦＵＮＤＳ建設・資材（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.193 [main] INFO  UpdateFundDiv - XX  JP3046590000  16200  0.100  ＮＥＸＴ　ＦＵＮＤＳ素材・化学（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.194 [main] INFO  UpdateFundDiv - XX  JP3046600007  16210  0.100  ＮＥＸＴ　ＦＵＮＤＳ医薬品（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.196 [main] INFO  UpdateFundDiv - XX  JP3046610006  16220  0.100  ＮＥＸＴ　ＦＵＮＤＳ自動車・輸送機（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.197 [main] INFO  UpdateFundDiv - XX  JP3046620005  16230  0.100  ＮＥＸＴ　ＦＵＮＤＳ鉄鋼・非鉄（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.199 [main] INFO  UpdateFundDiv - XX  JP3046630004  16240  0.100  ＮＥＸＴ　ＦＵＮＤＳ機械（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.201 [main] INFO  UpdateFundDiv - XX  JP3046640003  16250  0.100  ＮＥＸＴ　ＦＵＮＤＳ電機・精密（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.203 [main] INFO  UpdateFundDiv - XX  JP3046650002  16260  0.100  ＮＥＸＴ　ＦＵＮＤＳ情報通信・サービスその他（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.205 [main] INFO  UpdateFundDiv - XX  JP3046660001  16270  0.100  ＮＥＸＴ　ＦＵＮＤＳ電力・ガス（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.207 [main] INFO  UpdateFundDiv - XX  JP3046670000  16280  0.100  ＮＥＸＴ　ＦＵＮＤＳ運輸・物流（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.211 [main] INFO  UpdateFundDiv - XX  JP3046680009  16290  0.100  ＮＥＸＴ　ＦＵＮＤＳ商社・卸売（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.213 [main] INFO  UpdateFundDiv - XX  JP3046690008  16300  0.100  ＮＥＸＴ　ＦＵＮＤＳ小売（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.215 [main] INFO  UpdateFundDiv - XX  JP3046700005  16310  0.100  ＮＥＸＴ　ＦＵＮＤＳ銀行（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.216 [main] INFO  UpdateFundDiv - XX  JP3046710004  16320  0.100  ＮＥＸＴ　ＦＵＮＤＳ金融（除く銀行）（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.219 [main] INFO  UpdateFundDiv - XX  JP3046720003  16330  0.100  ＮＥＸＴ　ＦＵＮＤＳ不動産（ＴＯＰＩＸ－１７）上場投信
//		21:03:40.223 [main] INFO  UpdateFundDiv - XX  JP3046800003  13250  0.010  ＮＥＸＴ　ＦＵＮＤＳブラジル株式指数・ボベスパ連動型上場投信
//		21:03:40.225 [main] INFO  UpdateFundDiv - XX  JP3047010008  13430  0.010  ＮＥＸＴ　ＦＵＮＤＳ東証ＲＥＩＴ指数連動型上場投信
//		21:03:40.226 [main] INFO  UpdateFundDiv - XX  JP3047030006  13450  0.010  上場インデックスファンドＪリート（東証ＲＥＩＴ指数）隔月分配型
//		21:03:40.229 [main] INFO  UpdateFundDiv - XX  JP3047060003  13480  0.010  ＭＡＸＩＳトピックス上場投信
//		21:03:40.237 [main] INFO  UpdateFundDiv - XX  JP3047100007  16780  0.010  ＮＥＸＴ　ＦＵＮＤＳインド株式指数・Ｎｉｆｔｙ５０連動型上場投信
//		21:03:40.240 [main] INFO  UpdateFundDiv - XX  JP3047120005  16800  0.010  上場インデックスファンド海外先進国株式（ＭＳＣＩ－ＫＯＫＵＳＡＩ）
//		21:03:40.241 [main] INFO  UpdateFundDiv - XX  JP3047130004  16810  0.010  上場インデックスファンド海外新興国株式（ＭＳＣＩエマージング）
//		21:03:40.242 [main] INFO  UpdateFundDiv - XX  JP3047170000  16980  0.010  上場インデックスファンド日本高配当（東証配当フォーカス１００）
//		21:03:40.245 [main] INFO  UpdateFundDiv - XX  JP3047240001  15450  0.100  ＮＥＸＴ　ＦＵＮＤＳ　ＮＡＳＤＡＱ－１００（為替ヘッジなし）連動型上場投信
//		21:03:40.249 [main] INFO  UpdateFundDiv - XX  JP3047260009  15470  0.010  上場インデックスファンド米国株式（Ｓ＆Ｐ５００）
//		21:03:40.250 [main] INFO  UpdateFundDiv - XX  JP3047290006  15500  0.010  ＭＡＸＩＳ海外株式（ＭＳＣＩコクサイ）上場投信
//		21:03:40.255 [main] INFO  UpdateFundDiv - XX  JP3047330000  15540  0.010  上場インデックスファンド世界株式（ＭＳＣＩ　ＡＣＷＩ）除く日本
//		21:03:40.257 [main] INFO  UpdateFundDiv - XX  JP3047340009  15550  0.010  上場インデックスファンド豪州リート（Ｓ＆Ｐ／ＡＳＸ２００　Ａ－ＲＥＩＴ）
//		21:03:40.258 [main] INFO  UpdateFundDiv - XX  JP3047360007  15590  0.100  ＮＥＸＴ　ＦＵＮＤＳタイ株式ＳＥＴ５０指数連動型上場投信
//		21:03:40.260 [main] INFO  UpdateFundDiv - XX  JP3047370006  15600  0.100  ＮＥＸＴ　ＦＵＮＤＳ　ＦＴＳＥブルサ・マレーシアＫＬＣＩ連動型上場投信
//		21:03:40.276 [main] INFO  UpdateFundDiv - XX  JP3047570001  15780  0.100  上場インデックスファンド日経２２５（ミニ）
//		21:03:40.280 [main] INFO  UpdateFundDiv - XX  JP3047620004  15850  0.010  ｉＦｒｅｅＥＴＦ　ＴＯＰＩＸ　Ｅｘ－Ｆｉｎａｎｃｉａｌｓ
//		21:03:40.282 [main] INFO  UpdateFundDiv - XX  JP3047630003  15860  0.100  上場インデックスファンドＴＯＰＩＸ　Ｅｘ－Ｆｉｎａｎｃｉａｌｓ
//		21:03:40.285 [main] INFO  UpdateFundDiv - XX  JP3047680008  15920  0.100  上場インデックスファンドＪＰＸ日経インデックス４００
//		21:03:40.287 [main] INFO  UpdateFundDiv - XX  JP3047700004  15950  0.010  ＮＺＡＭ上場投信東証ＲＥＩＴ指数
//		21:03:40.290 [main] INFO  UpdateFundDiv - XX  JP3047710003  15960  0.010  ＮＺＡＭ上場投信ＴＯＰＩＸ　Ｅｘ－Ｆｉｎａｎｃｉａｌｓ
//		21:03:40.292 [main] INFO  UpdateFundDiv - XX  JP3047720002  15970  0.010  ＭＡＸＩＳ　Ｊリート上場投信
//		21:03:40.293 [main] INFO  UpdateFundDiv - XX  JP3047740000  15990  0.100  ｉＦｒｅｅＥＴＦ　ＪＰＸ日経４００
//		21:03:40.300 [main] INFO  UpdateFundDiv - XX  JP3047840008  13640  0.010  ｉシェアーズＪＰＸ日経４００ＥＴＦ
//		21:03:40.311 [main] INFO  UpdateFundDiv - XX  JP3047930007  13980  0.010  ＳＭＤＡＭ東証ＲＥＩＴ指数上場投信
//		21:03:40.320 [main] INFO  UpdateFundDiv - XX  JP3048090009  14730  0.010  Ｏｎｅ　ＥＴＦトピックス
//		21:03:40.324 [main] INFO  UpdateFundDiv - XX  JP3048120004  14750  0.001  ｉシェアーズ・コアＴＯＰＩＸ　ＥＴＦ
//		21:03:40.325 [main] INFO  UpdateFundDiv - XX  JP3048130003  14760  0.010  ｉシェアーズ・コアＪリートＥＴＦ
//		21:03:40.326 [main] INFO  UpdateFundDiv - XX  JP3048140002  14770  0.010  ｉシェアーズＭＳＣＩ日本株最小分散ＥＴＦ
//		21:03:40.328 [main] INFO  UpdateFundDiv - XX  JP3048150001  14780  0.010  ｉシェアーズＭＳＣＩジャパン高配当利回りＥＴＦ
//		21:03:40.329 [main] INFO  UpdateFundDiv - XX  JP3048170009  13990  0.010  上場インデックスファンドＭＳＣＩ日本株高配当低ボラティリティ
//		21:03:40.330 [main] INFO  UpdateFundDiv - XX  JP3048220002  14790  0.100  ｉＦｒｅｅＥＴＦ　ＭＳＣＩ日本株人材設備投資指数
//		21:03:40.332 [main] INFO  UpdateFundDiv - XX  JP3048240000  14810  0.100  上場インデックスファンド日本経済貢献株
//		21:03:40.333 [main] INFO  UpdateFundDiv - XX  JP3048250009  14820  0.010  ｉシェアーズ・コア米国債７－１０年ＥＴＦ（為替ヘッジあり）
//		21:03:40.335 [main] INFO  UpdateFundDiv - XX  JP3048260008  14830  0.010  ｉシェアーズＪＰＸ／Ｓ＆Ｐ設備・人材投資ＥＴＦ
//		21:03:40.336 [main] INFO  UpdateFundDiv - XX  JP3048270007  14840  0.010  Ｏｎｅ　ＥＴＦ　ＪＰＸ／Ｓ＆Ｐ設備・人材投資指数
//		21:03:40.344 [main] INFO  UpdateFundDiv - XX  JP3048350007  14880  0.010  ｉＦｒｅｅＥＴＦ東証ＲＥＩＴ指数
//		21:03:40.352 [main] INFO  UpdateFundDiv - XX  JP3048460004  14960  0.010  ｉシェアーズ米ドル建て投資適格社債ＥＴＦ（為替ヘッジあり）
//		21:03:40.353 [main] INFO  UpdateFundDiv - XX  JP3048470003  14970  0.010  ｉシェアーズ米ドル建てハイイールド社債ＥＴＦ（為替ヘッジあり）
//		21:03:40.354 [main] INFO  UpdateFundDiv - XX  JP3048490001  16510  0.010  ｉＦｒｅｅＥＴＦ　ＴＯＰＩＸ高配当４０指数
//		21:03:40.355 [main] INFO  UpdateFundDiv - XX  JP3048500007  16520  0.010  ｉＦｒｅｅＥＴＦ　ＭＳＣＩ日本株女性活躍指数（ＷＩＮ）
//		21:03:40.358 [main] INFO  UpdateFundDiv - XX  JP3048510006  16530  0.010  ｉＦｒｅｅＥＴＦ　ＭＳＣＩジャパンＥＳＧセレクト・リーダーズ指数
//		21:03:40.359 [main] INFO  UpdateFundDiv - XX  JP3048520005  16540  0.010  ｉＦｒｅｅＥＴＦ　ＦＴＳＥ　ＪＰＸ　Ｂｌｏｓｓｏｍ　Ｊａｐａｎ　Ｉｎｄｅｘ
//		21:03:40.360 [main] INFO  UpdateFundDiv - XX  JP3048530004  16550  0.001  ｉシェアーズＳ＆Ｐ５００米国株ＥＴＦ
//		21:03:40.361 [main] INFO  UpdateFundDiv - XX  JP3048540003  16560  0.001  ｉシェアーズ・コア米国債７－１０年ＥＴＦ
//		21:03:40.362 [main] INFO  UpdateFundDiv - XX  JP3048550002  16570  0.010  ｉシェアーズ・コアＭＳＣＩ先進国株（除く日本）ＥＴＦ
//		21:03:40.363 [main] INFO  UpdateFundDiv - XX  JP3048560001  16580  0.010  ｉシェアーズ・コアＭＳＣＩ新興国株ＥＴＦ
//		21:03:40.364 [main] INFO  UpdateFundDiv - XX  JP3048570000  16590  0.010  ｉシェアーズ米国リートＥＴＦ
//		21:03:40.370 [main] INFO  UpdateFundDiv - XX  JP3048610004  25100  0.010  ＮＥＸＴ　ＦＵＮＤＳ国内債券・ＮＯＭＵＲＡ－ＢＰＩ総合連動型上場投信
//		21:03:40.371 [main] INFO  UpdateFundDiv - XX  JP3048620003  25110  0.010  ＮＥＸＴ　ＦＵＮＤＳ外国債券・ＦＴＳＥ世界国債インデックス（除く日本・為替ヘッジなし）連動型上場投信
//		21:03:40.372 [main] INFO  UpdateFundDiv - XX  JP3048630002  25120  0.010  ＮＥＸＴ　ＦＵＮＤＳ外国債券・ＦＴＳＥ世界国債インデックス（除く日本・為替ヘッジあり）連動型上場投信
//		21:03:40.375 [main] INFO  UpdateFundDiv - XX  JP3048640001  25130  0.010  ＮＥＸＴ　ＦＵＮＤＳ外国株式・ＭＳＣＩ－ＫＯＫＵＳＡＩ指数（為替ヘッジなし）連動型上場投信
//		21:03:40.376 [main] INFO  UpdateFundDiv - XX  JP3048650000  25140  0.010  ＮＥＸＴ　ＦＵＮＤＳ外国株式・ＭＳＣＩ－ＫＯＫＵＳＡＩ指数（為替ヘッジあり）連動型上場投信
//		21:03:40.377 [main] INFO  UpdateFundDiv - XX  JP3048660009  25150  0.010  ＮＥＸＴ　ＦＵＮＤＳ外国ＲＥＩＴ・Ｓ＆Ｐ先進国ＲＥＩＴ指数（除く日本・為替ヘッジなし）連動型上場投信
//		21:03:40.381 [main] INFO  UpdateFundDiv - XX  JP3048710002  25170  0.010  ＭＡＸＩＳ　Ｊリート・コア上場投信
//		21:03:40.383 [main] INFO  UpdateFundDiv - XX  JP3048720001  25180  0.010  ＮＥＸＴ　ＦＵＮＤＳ　ＭＳＣＩ日本株女性活躍指数（セレクト）連動型上場投信
//		21:03:40.384 [main] INFO  UpdateFundDiv - XX  JP3048730000  25190  0.010  ＮＥＸＴ　ＦＵＮＤＳ新興国債券・Ｊ．Ｐ．モルガン・エマージング・マーケット・ボンド・インデックス・プラス（為替ヘッジなし）連動型上場投信
//		21:03:40.386 [main] INFO  UpdateFundDiv - XX  JP3048740009  25200  0.010  ＮＥＸＴ　ＦＵＮＤＳ新興国株式・ＭＳＣＩエマージング・マーケット・インデックス（為替ヘッジなし）連動型上場投信
//		21:03:40.387 [main] INFO  UpdateFundDiv - XX  JP3048760007  25210  0.010  上場インデックスファンド米国株式（Ｓ＆Ｐ５００）為替ヘッジあり
//		21:03:40.389 [main] INFO  UpdateFundDiv - XX  JP3048790004  25220  0.010  ｉシェアーズオートメーション＆ロボットＥＴＦ
//		21:03:40.390 [main] INFO  UpdateFundDiv - XX  JP3048800001  25230  0.010  ＭＡＸＩＳトピックス（除く金融）上場投信
//		21:03:40.393 [main] INFO  UpdateFundDiv - XX  JP3048830008  25240  0.010  ＮＺＡＭ上場投信ＴＯＰＩＸ
//		21:03:40.396 [main] INFO  UpdateFundDiv - XX  JP3048860005  25270  0.010  ＮＺＡＭ上場投信東証ＲＥＩＴ　Ｃｏｒｅ指数
//		21:03:40.398 [main] INFO  UpdateFundDiv - XX  JP3048870004  25280  0.010  ｉＦｒｅｅＥＴＦ東証ＲＥＩＴ　Ｃｏｒｅ指数
//		21:03:40.399 [main] INFO  UpdateFundDiv - XX  JP3048890002  25290  0.010  ＮＥＸＴ　ＦＵＮＤＳ野村株主還元７０連動型上場投信
//		21:03:40.402 [main] INFO  UpdateFundDiv - XX  JP3048910008  25520  0.100  上場インデックスファンドＪリート（東証ＲＥＩＴ指数）隔月分配型（ミニ）
//		21:03:40.407 [main] INFO  UpdateFundDiv - XX  JP3048930006  25540  0.010  ＮＥＸＴ　ＦＵＮＤＳブルームバーグ米国投資適格社債（１－１０年）インデックス（為替ヘッジあり）連動型上場投信
//		21:03:40.408 [main] INFO  UpdateFundDiv - XX  JP3048940005  25550  0.100  東証ＲＥＩＴ　ＥＴＦ
//		21:03:40.409 [main] INFO  UpdateFundDiv - XX  JP3048950004  25560  0.010  Ｏｎｅ　ＥＴＦ東証ＲＥＩＴ指数
//		21:03:40.412 [main] INFO  UpdateFundDiv - XX  JP3048970002  25570  0.010  ＳＭＤＡＭトピックス上場投信
//		21:03:40.414 [main] INFO  UpdateFundDiv - XX  JP3048980001  25580  10.000  ＭＡＸＩＳ米国株式（Ｓ＆Ｐ５００）上場投信
//		21:03:40.415 [main] INFO  UpdateFundDiv - XX  JP3048990000  25590  10.000  ＭＡＸＩＳ全世界株式（オール・カントリー）上場投信
//		21:03:40.417 [main] INFO  UpdateFundDiv - XX  JP3049020005  25610  0.010  ｉシェアーズ・コア日本国債ＥＴＦ
//		21:03:40.419 [main] INFO  UpdateFundDiv - XX  JP3049030004  25620  0.010  上場インデックスファンド米国株式（ダウ平均）為替ヘッジあり
//		21:03:40.420 [main] INFO  UpdateFundDiv - XX  JP3049040003  25630  0.001  ｉシェアーズＳ＆Ｐ５００米国株ＥＴＦ（為替ヘッジあり）
//		21:03:40.421 [main] INFO  UpdateFundDiv - XX  JP3049050002  25640  0.010  グローバルＸ　ＭＳＣＩスーパーディビィデンド－日本株式　ＥＴＦ
//		21:03:40.422 [main] INFO  UpdateFundDiv - XX  JP3049060001  25650  0.010  グローバルＸロジスティクス・Ｊ－ＲＥＩＴ　ＥＴＦ
//		21:03:40.424 [main] INFO  UpdateFundDiv - XX  JP3049070000  25660  0.010  上場インデックスファンド日経ＥＳＧリート
//		21:03:40.425 [main] INFO  UpdateFundDiv - XX  JP3049080009  25670  0.010  ＮＺＡＭ上場投信Ｓ＆Ｐ／ＪＰＸカーボン・エフィシェント指数
//		21:03:40.426 [main] INFO  UpdateFundDiv - XX  JP3049090008  25680  0.010  上場インデックスファンド米国株式（ＮＡＳＤＡＱ１００）為替ヘッジなし
//		21:03:40.429 [main] INFO  UpdateFundDiv - XX  JP3049100005  25690  0.010  上場インデックスファンド米国株式（ＮＡＳＤＡＱ１００）為替ヘッジあり
//		21:03:40.430 [main] INFO  UpdateFundDiv - XX  JP3049120003  26200  0.001  ｉシェアーズ米国債１－３年ＥＴＦ
//		21:03:40.431 [main] INFO  UpdateFundDiv - XX  JP3049130002  26210  0.010  ｉシェアーズ米国債２０年超ＥＴＦ（為替ヘッジあり）
//		21:03:40.433 [main] INFO  UpdateFundDiv - XX  JP3049140001  26220  0.010  ｉシェアーズ米ドル建て新興国債券ＥＴＦ（為替ヘッジあり）
//		21:03:40.434 [main] INFO  UpdateFundDiv - XX  JP3049150000  26230  0.010  ｉシェアーズユーロ建て投資適格社債ＥＴＦ（為替ヘッジあり）
//		21:03:40.436 [main] INFO  UpdateFundDiv - XX  JP3049160009  26240  0.100  ｉＦｒｅｅＥＴＦ日経２２５（年４回決算型）
//		21:03:40.436 [main] INFO  UpdateFundDiv - XX  JP3049170008  26250  0.100  ｉＦｒｅｅＥＴＦ　ＴＯＰＩＸ（年４回決算型）
//		21:03:40.441 [main] INFO  UpdateFundDiv - XX  JP3049210002  26260  0.010  グローバルＸデジタル・イノベーション－日本株式ＥＴＦ
//		21:03:40.443 [main] INFO  UpdateFundDiv - XX  JP3049220001  26270  0.010  グローバルＸ　ｅコマース－日本株式ＥＴＦ
//		21:03:40.447 [main] INFO  UpdateFundDiv - XX  JP3049250008  26330  0.010  ＮＥＸＴ　ＦＵＮＤＳ　Ｓ＆Ｐ５００指数（為替ヘッジなし）連動型上場投信
//		21:03:40.449 [main] INFO  UpdateFundDiv - XX  JP3049260007  26340  0.010  ＮＥＸＴ　ＦＵＮＤＳ　Ｓ＆Ｐ５００指数（為替ヘッジあり）連動型上場投信
//		21:03:40.450 [main] INFO  UpdateFundDiv - XX  JP3049280005  26360  0.010  グローバルＸ　ＭＳＣＩガバナンス・クオリティ－日本株式ＥＴＦ
//		21:03:40.452 [main] INFO  UpdateFundDiv - XX  JP3049290004  26370  0.010  グローバルＸクリーンテック－日本株式ＥＴＦ
//		21:03:40.453 [main] INFO  UpdateFundDiv - XX  JP3049300001  26380  0.010  グローバルＸロボティクス＆ＡＩ－日本株式ＥＴＦ
//		21:03:40.455 [main] INFO  UpdateFundDiv - XX  JP3049310000  26390  0.010  グローバルＸバイオ＆メドテック－日本株式ＥＴＦ
//		21:03:40.456 [main] INFO  UpdateFundDiv - XX  JP3049320009  26400  0.010  グローバルＸゲーム＆アニメ－日本株式ＥＴＦ
//		21:03:40.457 [main] INFO  UpdateFundDiv - XX  JP3049330008  26410  0.010  グローバルＸグローバルリーダーズ－日本株式ＥＴＦ
//		21:03:40.462 [main] INFO  UpdateFundDiv - XX  JP3049360005  26440  0.010  グローバルＸ半導体関連－日本株式ＥＴＦ
//		21:03:40.464 [main] INFO  UpdateFundDiv - XX  JP3049370004  26450  0.010  グローバルＸレジャー＆エンターテインメント－日本株式ＥＴＦ
//		21:03:40.465 [main] INFO  UpdateFundDiv - XX  JP3049380003  26460  0.010  グローバルＸメタルビジネス－日本株式ＥＴＦ
//		21:03:40.466 [main] INFO  UpdateFundDiv - XX  JP3049390002  26470  0.100  ＮＥＸＴ　ＦＵＮＤＳブルームバーグ米国国債（７－１０年）インデックス（為替ヘッジなし）連動型上場投信
//		21:03:40.467 [main] INFO  UpdateFundDiv - XX  JP3049400009  26480  0.100  ＮＥＸＴ　ＦＵＮＤＳブルームバーグ米国国債（７－１０年）インデックス（為替ヘッジあり）連動型上場投信
//		21:03:40.469 [main] INFO  UpdateFundDiv - XX  JP3049410008  28360  0.010  グローバルＸフィンテック－日本株式ＥＴＦ
//		21:03:40.470 [main] INFO  UpdateFundDiv - XX  JP3049420007  28370  0.010  グローバルＸ中小型リーダーズ－日本株式ＥＴＦ
//		21:03:40.473 [main] INFO  UpdateFundDiv - XX  JP3049450004  26490  0.001  ｉシェアーズ米国政府系機関ジニーメイＭＢＳ　ＥＴＦ（為替ヘッジあり）
//		21:03:40.474 [main] INFO  UpdateFundDiv - XX  JP3049460003  28400  0.010  ｉＦｒｅｅＥＴＦ　ＮＡＳＤＡＱ１００（為替ヘッジなし）
//		21:03:40.477 [main] INFO  UpdateFundDiv - XX  JP3049470002  28410  0.010  ｉＦｒｅｅＥＴＦ　ＮＡＳＤＡＱ１００（為替ヘッジあり）
//		21:03:40.478 [main] INFO  UpdateFundDiv - XX  JP3049490000  28430  0.100  上場インデックスファンド豪州国債（為替ヘッジあり）
//		21:03:40.480 [main] INFO  UpdateFundDiv - XX  JP3049500006  28440  0.100  上場インデックスファンド豪州国債（為替ヘッジなし）
//		21:03:40.481 [main] INFO  UpdateFundDiv - XX  JP3049520004  28450  0.010  ＮＥＸＴ　ＦＵＮＤＳ　ＮＡＳＤＡＱ－１００（為替ヘッジあり）連動型上場投信
//		21:03:40.482 [main] INFO  UpdateFundDiv - XX  JP3049530003  28460  0.010  ＮＥＸＴ　ＦＵＮＤＳダウ・ジョーンズ工業株３０種平均株価（為替ヘッジあり）連動型上場投信
//		21:03:40.483 [main] INFO  UpdateFundDiv - XX  JP3049540002  28470  0.010  グローバルＸ新成長インフラ－日本株式ＥＴＦ
//		21:03:40.484 [main] INFO  UpdateFundDiv - XX  JP3049550001  28480  0.010  グローバルＸ　ＭＳＣＩ気候変動対応－日本株式ＥＴＦ
//		21:03:40.485 [main] INFO  UpdateFundDiv - XX  JP3049560000  28490  0.010  グローバルＸ　Ｍｏｒｎｉｎｇｓｔａｒ高配当ＥＳＧ－日本株式ＥＴＦ
//		21:03:40.486 [main] INFO  UpdateFundDiv - XX  JP3049580008  28510  0.001  ｉシェアーズＭＳＣＩジャパンＳＲＩ　ＥＴＦ
//		21:03:40.488 [main] INFO  UpdateFundDiv - XX  JP3049590007  28520  0.001  ｉシェアーズグリーンＪリートＥＴＦ
//		21:03:40.489 [main] INFO  UpdateFundDiv - XX  JP3049600004  28530  0.001  ｉシェアーズ気候リスク調整世界国債ＥＴＦ（除く日本・為替ヘッジあり）
//		21:03:40.492 [main] INFO  UpdateFundDiv - XX  JP3049610003  28540  0.010  グローバルＸテック・トップ２０－日本株式ＥＴＦ
//		21:03:40.493 [main] INFO  UpdateFundDiv - XX  JP3049620002  28550  0.010  グローバルＸグリーン・Ｊ－ＲＥＩＴ　ＥＴＦ
//		21:03:40.494 [main] INFO  UpdateFundDiv - XX  JP3049630001  28560  0.001  ｉシェアーズ米国債３－７年ＥＴＦ（為替ヘッジあり）
//		21:03:40.495 [main] INFO  UpdateFundDiv - XX  JP3049640000  28570  0.001  ｉシェアーズドイツ国債ＥＴＦ（為替ヘッジあり）
//		21:03:40.496 [main] INFO  UpdateFundDiv - XX  JP3049650009  28580  0.010  グローバルＸ日経２２５カバード・コールＥＴＦ（プレミアム再投資型）
//		21:03:40.498 [main] INFO  UpdateFundDiv - XX  JP3049660008  28590  0.010  ＮＥＸＴ　ＦＵＮＤＳユーロ・ストックス５０指数（為替ヘッジあり）連動型上場投信
//		21:03:40.499 [main] INFO  UpdateFundDiv - XX  JP3049670007  28600  0.010  ＮＥＸＴ　ＦＵＮＤＳドイツ株式・ＤＡＸ（為替ヘッジあり）連動型上場投信
//		21:03:40.500 [main] INFO  UpdateFundDiv - XX  JP3049680006  28610  0.100  上場インデックスファンドフランス国債（為替ヘッジなし）
//		21:03:40.501 [main] INFO  UpdateFundDiv - XX  JP3049690005  28620  0.100  上場インデックスファンドフランス国債（為替ヘッジあり）
//		21:03:40.502 [main] INFO  UpdateFundDiv - XX  JP3049710001  28640  0.010  グローバルＸロジスティクス・ＲＥＩＴ　ＥＴＦ
//		21:03:40.503 [main] INFO  UpdateFundDiv - XX  JP3049720000  28650  0.010  グローバルＸ　ＮＡＳＤＡＱ１００・カバード・コールＥＴＦ
//		21:03:40.504 [main] INFO  UpdateFundDiv - XX  JP3049730009  28660  0.010  グローバルＸ米国優先証券ＥＴＦ
//		21:03:40.508 [main] INFO  UpdateFundDiv - XX  JP3049750007  28680  0.010  グローバルＸ　Ｓ＆Ｐ５００・カバード・コールＥＴＦ
//		21:03:40.509 [main] INFO  UpdateFundDiv - XX  JP3049760006  28690  2.500  ｉＦｒｅｅＥＴＦ　ＮＡＳＤＡＱ１００レバレッジ
//		21:03:40.510 [main] INFO  UpdateFundDiv - XX  JP3049770005  28700  0.100  ｉＦｒｅｅＥＴＦ　ＮＡＳＤＡＱ１００ダブルインバース
//		21:03:40.511 [main] INFO  UpdateFundDiv - XX  JP3049780004  22350  0.010  上場インデックスファンド米国株式（ダウ平均）為替ヘッジなし
//		21:03:40.513 [main] INFO  UpdateFundDiv - XX  JP3049790003  22360  0.010  グローバルＸ　Ｓ＆Ｐ５００配当貴族ＥＴＦ
//		21:03:40.514 [main] INFO  UpdateFundDiv - XX  JP3049800000  22370  5.000  ｉＦｒｅｅＥＴＦ　Ｓ＆Ｐ５００レバレッジ
//		21:03:40.518 [main] INFO  UpdateFundDiv - XX  JP3049840006  22410  0.010  ＭＡＸＩＳ　ＮＹダウ上場投信
//		21:03:40.520 [main] INFO  UpdateFundDiv - XX  JP3049850005  22420  0.010  ＭＡＸＩＳ　ＮＹダウ上場投信（為替ヘッジあり）
//		21:03:40.523 [main] INFO  UpdateFundDiv - XX  JP3049860004  22430  0.010  グローバルＸ半導体ＥＴＦ
//		21:03:40.524 [main] INFO  UpdateFundDiv - XX  JP3049880002  22450  0.010  ＮＥＸＴ　ＦＵＮＤＳブルームバーグ・ドイツ国債（７－１０年）インデックス（為替ヘッジあり）連動型上場投信
//		21:03:40.525 [main] INFO  UpdateFundDiv - XX  JP3049900008  22470  0.010  ｉＦｒｅｅＥＴＦ　Ｓ＆Ｐ５００（為替ヘッジなし）
//		21:03:40.526 [main] INFO  UpdateFundDiv - XX  JP3049910007  22480  0.010  ｉＦｒｅｅＥＴＦ　Ｓ＆Ｐ５００（為替ヘッジあり）
//		21:03:40.527 [main] INFO  UpdateFundDiv - XX  JP3049920006  22490  0.100  ｉＦｒｅｅＥＴＦ　Ｓ＆Ｐ５００ダブルインバース
//		21:03:40.528 [main] INFO  UpdateFundDiv - XX  JP3049930005  22500  0.001  ｉシェアーズＭＳＣＩジャパン気候変動アクションＥＴＦ
//		21:03:40.531 [main] INFO  UpdateFundDiv - XX  JP3049950003  22520  0.010  グローバルＸ　Ｍｏｒｎｉｎｇｓｔａｒ米国中小型Ｍｏａｔ　ＥＴＦ
//		21:03:40.532 [main] INFO  UpdateFundDiv - XX  JP3049960002  22530  0.010  グローバルＸスーパーディビィデンド－ＵＳ　ＥＴＦ
//		21:03:40.539 [main] INFO  UpdateFundDiv - XX  JP3050010002  20830  0.010  ＮＥＸＴ　ＦＵＮＤＳ日本成長株アクティブ上場投信
//		21:03:40.540 [main] INFO  UpdateFundDiv - XX  JP3050020001  20840  0.010  ＮＥＸＴ　ＦＵＮＤＳ日本高配当株アクティブ上場投信
//		21:03:40.541 [main] INFO  UpdateFundDiv - XX  JP3050030000  20850  0.010  ＭＡＸＩＳ高配当日本株アクティブ上場投信
//		21:03:40.543 [main] INFO  UpdateFundDiv - XX  JP3050040009  20860  0.010  ＮＺＡＭ上場投信Ｓ＆Ｐ５００（為替ヘッジあり）
//		21:03:40.544 [main] INFO  UpdateFundDiv - XX  JP3050050008  20870  0.010  ＮＺＡＭ上場投信ＮＡＳＤＡＱ１００（為替ヘッジあり）
//		21:03:40.545 [main] INFO  UpdateFundDiv - XX  JP3050060007  20880  0.010  ＮＺＡＭ上場投信ＮＹダウ３０（為替ヘッジあり）
//		21:03:40.546 [main] INFO  UpdateFundDiv - XX  JP3050070006  20890  0.010  ＮＺＡＭ上場投信ＤＡＸ（為替ヘッジあり）
//		21:03:40.547 [main] INFO  UpdateFundDiv - XX  JP3050080005  20900  0.010  ＮＺＡＭ上場投信米国国債７－１０年（為替ヘッジあり）
//		21:03:40.548 [main] INFO  UpdateFundDiv - XX  JP3050090004  20910  0.010  ＮＺＡＭ上場投信ドイツ国債７－１０年（為替ヘッジあり）
//		21:03:40.549 [main] INFO  UpdateFundDiv - XX  JP3050100001  20920  0.010  ＮＺＡＭ上場投信フランス国債７－１０年（為替ヘッジあり）
//		21:03:40.552 [main] INFO  UpdateFundDiv - XX  JP3050110000  20930  0.100  上場Ｔｒａｃｅｒｓ米国債０－２年ラダー（為替ヘッジなし）
//		21:03:40.554 [main] INFO  UpdateFundDiv - XX  JP3050130008  20950  0.010  グローバルＸ　Ｓ＆Ｐ５００配当貴族ＥＴＦ（為替ヘッジあり）
//		21:03:40.556 [main] INFO  UpdateFundDiv - XX  JP3050140007  20960  0.010  グローバルＸオフィス・Ｊ－ＲＥＩＴ　ＥＴＦ
//		21:03:40.557 [main] INFO  UpdateFundDiv - XX  JP3050150006  20970  0.010  グローバルＸレジデンシャル・Ｊ－ＲＥＩＴ　ＥＴＦ
//		21:03:40.558 [main] INFO  UpdateFundDiv - XX  JP3050160005  20980  0.010  グローバルＸホテル＆リテール・Ｊ－ＲＥＩＴ　ＥＴＦ
//		21:03:40.559 [main] INFO  UpdateFundDiv - XX  JP3050170004  22550  0.001  ｉシェアーズ米国債２０年超ＥＴＦ
//		21:03:40.560 [main] INFO  UpdateFundDiv - XX  JP3050180003  22560  0.001  ｉシェアーズ米国総合債券ＥＴＦ
//		21:03:40.561 [main] INFO  UpdateFundDiv - XX  JP3050190002  22570  0.001  ｉシェアーズ米ドル建て投資適格社債ＥＴＦ
//		21:03:40.561 [main] INFO  UpdateFundDiv - XX  JP3050200009  22580  0.001  ｉシェアーズ米ドル建てハイイールド社債ＥＴＦ
//		21:03:40.562 [main] INFO  UpdateFundDiv - XX  JP3050210008  22590  0.001  ｉシェアーズフランス国債７－１０年ＥＴＦ（為替ヘッジあり）
//		21:03:40.564 [main] INFO  UpdateFundDiv - XX  JP3050220007  20110  0.010  ＳＭＤＡＭ　Ａｃｔｉｖｅ　ＥＴＦ日本高配当株式
//		21:03:40.568 [main] INFO  UpdateFundDiv - XX  JP3050240005  20130  0.001  ｉシェアーズ米国高配当株ＥＴＦ
//		21:03:40.583 [main] INFO  UpdateFundDiv - XX  JP3050250004  20140  0.001  ｉシェアーズ米国連続増配株ＥＴＦ
//		21:03:40.591 [main] INFO  UpdateFundDiv - XX  JP3050260003  20150  0.010  ｉＦｒｅｅＥＴＦ米国国債７－１０年（為替ヘッジなし）
//		21:03:40.592 [main] INFO  UpdateFundDiv - XX  JP3050270002  20160  0.010  ｉＦｒｅｅＥＴＦ米国国債７－１０年（為替ヘッジあり）
//		21:03:40.593 [main] INFO  UpdateFundDiv - XX  JP3050280001  20170  0.010  ｉＦｒｅｅＥＴＦ　ＪＰＸプライム１５０
//		21:03:40.595 [main] INFO  UpdateFundDiv - XX  JP3050290000  20180  0.010  グローバルＸ　ＵＳ　ＲＥＩＴ・トップ２０ＥＴＦ
//		21:03:40.596 [main] INFO  UpdateFundDiv - XX  JP3050300007  20190  0.010  グローバルＸ米国優先証券ＥＴＦ（隔月分配型）
//		21:03:40.597 [main] INFO  UpdateFundDiv - XX  JP3050310006  133A0  0.010  グローバルＸ超短期米国債ＥＴＦ
//		21:03:40.600 [main] INFO  UpdateFundDiv - XX  JP3050330004  159A0  0.010  ＮＥＸＴ　ＦＵＮＤＳ　ＪＰＸプライム１５０指数連動型上場投信
//		21:03:40.604 [main] INFO  UpdateFundDiv - XX  JP3050350002  178A0  0.010  グローバルＸ革新的優良企業ＥＴＦ
//		21:03:40.606 [main] INFO  UpdateFundDiv - XX  JP3050360001  179A0  0.010  グローバルＸ超長期米国債ＥＴＦ（為替ヘッジあり）
//		21:03:40.607 [main] INFO  UpdateFundDiv - XX  JP3050370000  180A0  0.010  グローバルＸ超長期米国債ＥＴＦ
//		21:03:40.609 [main] INFO  UpdateFundDiv - XX  JP3050380009  181A0  0.010  ＭＡＸＩＳ米国国債１－３年上場投信（為替ヘッジなし）
//		21:03:40.610 [main] INFO  UpdateFundDiv - XX  JP3050390008  182A0  0.010  ＭＡＸＩＳ米国国債２０年超上場投信（為替ヘッジなし）
//		21:03:40.611 [main] INFO  UpdateFundDiv - XX  JP3050400005  183A0  0.010  ＭＡＸＩＳ米国国債２０年超上場投信（為替ヘッジあり）
//		21:03:40.612 [main] INFO  UpdateFundDiv - XX  JP3050410004  188A0  0.010  グローバルＸインド・トップ１０＋ＥＴＦ
//		21:03:40.613 [main] INFO  UpdateFundDiv - XX  JP3050420003  200A0  0.010  ＮＥＸＴ　ＦＵＮＤＳ日経半導体株指数連動型上場投信
//		21:03:40.614 [main] INFO  UpdateFundDiv - XX  JP3050430002  201A0  0.001  ｉシェアーズＮｉｆｔｙ５０インド株ＥＴＦ
//		21:03:40.616 [main] INFO  UpdateFundDiv - XX  JP3050440001  210A0  0.010  ｉＦｒｅｅＥＴＦ日経高利回りＲＥＩＴ指数
//		21:03:40.617 [main] INFO  UpdateFundDiv - XX  JP3050450000  213A0  0.010  上場インデックスファンド日経半導体株
//		21:03:40.619 [main] INFO  UpdateFundDiv - XX  JP3050460009  221A0  0.010  ＭＡＸＩＳ日経半導体株上場投信
//		21:03:40.620 [main] INFO  UpdateFundDiv - XX  JP3050470008  223A0  0.010  グローバルＸ　ＡＩ＆ビッグデータＥＴＦ
//		21:03:40.621 [main] INFO  UpdateFundDiv - XX  JP3050480007  224A0  0.010  グローバルＸウラニウムビジネスＥＴＦ
//		21:03:40.622 [main] INFO  UpdateFundDiv - XX  JP3050490006  233A0  0.010  ｉＦｒｅｅＥＴＦインドＮｉｆｔｙ５０
//		21:03:40.624 [main] INFO  UpdateFundDiv - XX  JP3050500002  234A0  0.010  グローバルＸ　ＭＳＣＩキャッシュフローキング－日本株式ＥＴＦ
//		21:03:40.625 [main] INFO  UpdateFundDiv - XX  JP3050510001  235A0  0.010  グローバルＸ高配当３０－日本株式ＥＴＦ
//		21:03:40.627 [main] INFO  UpdateFundDiv - XX  JP3050520000  236A0  0.001  ｉシェアーズ日本国債７－１０年ＥＴＦ
//		21:03:40.628 [main] INFO  UpdateFundDiv - XX  JP3050530009  237A0  0.001  ｉシェアーズ米国債２５年超ロングデュレーションＥＴＦ
//		21:03:40.630 [main] INFO  UpdateFundDiv - XX  JP3050540008  238A0  0.001  ｉシェアーズ米国債２５年超ロングデュレーションＥＴＦ（為替ヘッジあり）
//		21:03:40.635 [main] INFO  UpdateFundDiv - XX  JP3050580004  282A0  0.010  グローバルＸ半導体・トップ１０－日本株式ＥＴＦ
//		21:03:40.637 [main] INFO  UpdateFundDiv - XX  JP3050590003  283A0  0.010  グローバルＸ　ＵＳテック・配当貴族ＥＴＦ
//		21:03:40.638 [main] INFO  UpdateFundDiv - XX  JP3050600000  294A0  0.010  ＮＥＸＴ　ＦＵＮＤＳ　ＭＳＣＩジャパン気候変動指数（セレクト）連動型上場投信
//		21:03:40.641 [main] INFO  UpdateFundDiv - XX  JP3050620008  313A0  0.001  ｉシェアーズＳ＆Ｐ５００トップ２０ＥＴＦ
//		21:03:40.643 [main] INFO  UpdateFundDiv - XX  JP3050640006  315A0  0.010  グローバルＸ銀行高配当－日本株式ＥＴＦ
//		21:03:40.646 [main] INFO  UpdateFundDiv - XX  JP3050670003  328A0  0.010  グローバルＸプライシングパワー・リーダーズ－日本株式ＥＴＦ
//		21:03:40.648 [main] INFO  UpdateFundDiv - XX  JP3050690001  348A0  0.010  ＭＡＸＩＳ読売３３３日本株上場投信
//		21:03:40.650 [main] INFO  UpdateFundDiv - XX  JP3050700008  349A0  0.010  ＳＭＤＡＭ　Ａｃｔｉｖｅ　ＥＴＦ日本グロース株式
//		21:03:40.651 [main] INFO  UpdateFundDiv - XX  JP3050710007  354A0  0.010  ｉＦｒｅｅＥＴＦブルームバーグ日本株高配当５０指数
//		21:03:40.652 [main] INFO  UpdateFundDiv - XX  JP3050720006  356A0  0.010  グローバルＸ　Ｓ＆Ｐ５００キャッシュフロー・トップ１００ＥＴＦ
//		21:03:40.652 [main] INFO  UpdateFundDiv - XX  JP3050730005  360A0  0.010  東証ＲＥＩＴ　Ｃｏｒｅ　ＥＴＦ
//		21:03:40.653 [main] INFO  UpdateFundDiv - XX  JP3050740004  363A0  0.010  ｉＦｒｅｅＥＴＦ英国ＦＴＳＥ１００
//		21:03:40.654 [main] INFO  UpdateFundDiv - XX  JP3050750003  364A0  0.010  ＮＥＸＴ　ＦＵＮＤＳ　Ｓ＆Ｐ５００配当貴族指数連動型上場投信
//		21:03:40.655 [main] INFO  UpdateFundDiv - XX  JP3050760002  376A0  0.010  ＮＥＸＴ　ＦＵＮＤＳブルームバーグ米国国債（７－１０年）インデックス（７５％為替ヘッジあり）連動型上場投信
//		21:03:40.656 [main] INFO  UpdateFundDiv - XX  JP3050770001  379A0  0.010  グローバルＸ　Ｓ＆Ｐ５００ＥＴＦ（ダイナミック・プロテクション）
//		21:03:40.657 [main] INFO  UpdateFundDiv - XX  JP3050780000  380A0  0.010  グローバルＸチャイナテックＥＴＦ
//		21:03:40.659 [main] INFO  UpdateFundDiv - XX  JP3050790009  381A0  0.010  ｉＦｒｅｅＥＴＦ米国国債３－５年（為替ヘッジなし）
//		21:03:40.660 [main] INFO  UpdateFundDiv - XX  JP3050800006  382A0  0.010  ｉＦｒｅｅＥＴＦ米国国債３－５年（為替ヘッジあり）
//		21:03:40.661 [main] INFO  UpdateFundDiv - XX  JP3050810005  383A0  0.100  ＭＡＸＩＳ　Ｓ＆Ｐ５００均等ウェイト上場投信
//		21:03:40.661 [main] INFO  UpdateFundDiv - XX  JP3050820004  392A0  0.001  ｉシェアーズＮＡＳＤＡＱトップ３０ＥＴＦ
//		21:03:40.663 [main] INFO  UpdateFundDiv - XX  JP3050840002  395A0  0.010  業界改革厳選ＥＴＦ地銀
//		21:03:40.664 [main] INFO  UpdateFundDiv - XX  JP3050850001  396A0  0.010  業界改革厳選ＥＴＦ　ＲＥＩＴイベント・ドリブン
//		21:03:40.666 [main] INFO  UpdateFundDiv - XX  JP3050860000  399A0  0.010  上場インデックスファンド日経平均高配当株５０
//		21:03:40.668 [main] INFO  UpdateFundDiv - XX  JP3050890007  408A0  0.001  ｉシェアーズＡＩグローバル・イノベーションアクティブＥＴＦ
//		21:03:40.670 [main] INFO  UpdateFundDiv - XX  JP3050900004  412A0  0.010  ＮＥＸＴ　ＦＵＮＤＳ　ＴＩＰ　ＦａｃｔＳｅｔ台湾イノベイティブ・テクノロジー５０指数連動型上場投信
//		21:03:40.672 [main] INFO  UpdateFundDiv - XX  JP3050910003  413A0  0.010  ｉＦｒｅｅＥＴＦキャセイ台湾テックリーダー指数
//		21:03:40.676 [main] INFO  UpdateFundDiv - XX  JP3050940000  426A0  0.010  ニッセイＥＴＦ　Ｓ＆Ｐ５００イコール・ウェイト（為替ヘッジなし）
//		21:03:40.680 [main] INFO  UpdateFundDiv - XX  JP3050950009  435A0  0.010  ｉＦｒｅｅＥＴＦ日本株配当ローテーション戦略
//		21:03:40.687 [main] INFO  UpdateFundDiv - XX  JP3050960008  443A0  0.010  ｉＦｒｅｅＥＴＦ東証ＲＥＩＴ指数（２・５・８・１１月決算型）
//		21:03:40.865 [main] INFO  UpdateFundDiv - XX  JP3050990005  449A0  0.010  ステート・ストリート・スパイダーＳ＆Ｐ５００ＥＴＦ（為替ヘッジなし）
//		21:03:40.956 [main] INFO  UpdateFundDiv - XX  JP3051000002  450A0  0.010  ステート・ストリート・スパイダーＳ＆Ｐ５００ＥＴＦ（為替ヘッジあり）
//		21:03:41.000 [main] INFO  UpdateFundDiv - XX  JP3051010001  451A0  0.010  ステート・ストリート・スパイダーＳ＆Ｐ５００高配当株ＥＴＦ
//		21:03:41.084 [main] INFO  UpdateFundDiv - XX  JP3051020000  452A0  0.001  ｉシェアーズＳ＆Ｐ５００プレミアムインカムＥＴＦ
//		21:03:41.161 [main] INFO  UpdateFundDiv - XX  JP3051030009  453A0  0.001  ｉシェアーズ米国債２０年超プレミアムインカムＥＴＦ
//		21:03:41.186 [main] INFO  UpdateFundDiv - XX  JP3051040008  459A0  0.010  野村高利回りＪリート指数ＥＴＦ
//		21:03:41.198 [main] INFO  UpdateFundDiv - XX  JP3051050007  461A0  0.100  ＭＡＸＩＳ日本株高配当ＳＭＡＲＴ５０上場投信
//		21:03:41.214 [main] INFO  UpdateFundDiv - XX  JP3051060006  465A0  0.010  グローバルＸ日経平均株主還元４０－日本株式ＥＴＦ
//		21:03:41.215 [main] INFO  UpdateFundDiv - XX  JP3051070005  466A0  0.010  グローバルＸ防衛テックＥＴＦ
//		21:03:41.216 [main] INFO  UpdateFundDiv - XX  JP3051080004  467A0  0.010  グローバルＸ米ドル建て投資適格社債ＥＴＦ（為替ヘッジあり）
//		21:03:41.217 [main] INFO  UpdateFundDiv - XX  JP3051090003  468A0  0.010  グローバルＸ米ドル建て投資適格社債ＥＴＦ
//		21:03:41.219 [main] INFO  UpdateFundDiv - XX  JP3051100000  473A0  0.010  ニッセイＥＴＦ日経２２５インデックス
//		21:03:41.220 [main] INFO  UpdateFundDiv - XX  JP3051110009  486A0  0.010  ＮＥＸＴ　ＦＵＮＤＳユーロ・ストックス５０指数（為替ヘッジなし）連動型上場投信
//		21:03:41.222 [main] INFO  UpdateFundDiv - XX  JP3051120008  487A0  0.010  ＮＥＸＴ　ＦＵＮＤＳドイツ株式・ＤＡＸ（為替ヘッジなし）連動型上場投信
//		21:03:41.227 [main] INFO  UpdateFundDiv - XX  JP3051140006  489A0  0.010  東証ＲＥＩＴ物流フォーカスＥＴＦ
//		21:03:41.229 [main] INFO  UpdateFundDiv - XX  JP3051150005  491A0  0.001  ｉシェアーズＳ＆Ｐ５００除く金融ＥＴＦ（為替ヘッジあり）
//		21:03:41.230 [main] INFO  UpdateFundDiv - XX  JP3051160004  492A0  0.100  Ｏｎｅ　ＥＴＦ日本国債高クーポン（平均残存１０年未満）
//		21:03:41.234 [main] INFO  UpdateFundDiv - XX  JP3051190001  495A0  0.100  Ｏｎｅ　ＥＴＦ日本国債７－１０年
//		21:03:41.235 [main] INFO  UpdateFundDiv - XX  JP3051200008  496A0  0.100  Ｏｎｅ　ＥＴＦ日本国債１７－２０年
//		21:03:41.236 [main] INFO  UpdateFundDiv - XX  JP3051210007  502A0  0.010  グローバルＸ超短期円建て債券ＥＴＦ
//		21:03:41.238 [main] INFO  UpdateFundDiv - XX  JP3051250003  515A0  0.001  ｉシェアーズ高格付け日本円社債ＥＴＦ
//		21:03:41.239 [main] INFO  UpdateFundDiv - XX  JP3051260002  516A0  0.010  ｉＦｒｅｅＥＴＦ米ドル・ブル（１倍）
//		21:03:41.242 [main] INFO  UpdateFundDiv - XX  JP3051270001  517A0  0.010  ｉＦｒｅｅＥＴＦ米ドル・ベア（１倍）
//		21:03:41.243 [main] INFO  UpdateFundDiv - XX  JP3051280000  518A0  0.010  ＮＥＸＴ　ＦＵＮＤＳ　ＦＴＳＥ日本株高配当キャッシュフロー５０指数連動型上場投信
//		21:03:41.246 [main] INFO  UpdateFundDiv - XX  JP3051310005  530A0  0.010  ＮＺＡＭ上場投信東証ＲＥＩＴ指数（２・５・８・１１月決算型）
//		21:03:41.254 [main] INFO  UpdateFundDiv - XX  JP3051400004  539A0  0.010  ＮＺＡＭ上場投信海外債券（ＦＴＳＥ　ＷＧＢＩ除く日本）（為替ヘッジなし）
//		21:03:41.256 [main] INFO  UpdateFundDiv - XX  JP3051420002  541A0  0.010  Ｏｎｅ　ＥＴＦ　ＴＯＰＩＸ高配当株グロース指数
//		21:03:41.257 [main] INFO  UpdateFundDiv - XX  JP3051430001  552A0  0.100  ＭＡＸＩＳ米国ＡＩインフラ株上場投信
//		21:03:41.257 [main] INFO  UpdateFundDiv - XX  JP3051440000  563A0  0.010  グローバルＸ　ＮＡＳＤＡＱ１００・デイリー・カバード・コールＥＴＦ
//		21:03:41.259 [main] INFO  UpdateFundDiv - XX  JP3051460008  566A0  0.010  ｉＦｒｅｅＥＴＦブルームバーグ日本株（除く金融）高配当５０指数
//		21:03:41.265 [main] INFO  UpdateFundDiv - XX  JP3051530008  576A0  0.010  グローバルＸチャイナテック・カバード・コールＥＴＦ
//		21:03:46.727 [main] INFO  UpdateFundDiv - countA  5431
//		21:03:46.728 [main] INFO  UpdateFundDiv - countB  39
//		21:03:46.728 [main] INFO  UpdateFundDiv - countC  55
//		21:03:46.728 [main] INFO  UpdateFundDiv - countD  3
//		21:03:46.728 [main] INFO  UpdateFundDiv - countE  0
//		21:03:46.728 [main] INFO  UpdateFundDiv - countF  45
//		21:03:46.728 [main] INFO  UpdateFundDiv - countG  280

		var stockInfoMap = StorageMoneybu.StockInfoMoneybu.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));

		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		int countE = 0;
		int countF = 0;
		int countG = 0;

		var fundInfoList = StorageFundJP.FundInfo.getList();
		logger.info("fundInfoList  {}", fundInfoList.size());

		for(var fundInfo: fundInfoList) {
			var isinCode  = fundInfo.isinCode;
			var stockCode = fundInfo.stockCode;
			var name      = fundInfo.name;

			var divList   = StorageJITA.FundDiv.getList(isinCode);
			var stockInfo = stockInfoMap.get(isinCode);

			if (stockInfo == null) {
				countA++;
			} else if (divList.isEmpty()) {
				countB++;
			} else if (allZero(divList)) {
				countC++;
			} else if (!stockInfo.hasDivDate() || !stockInfo.hasDivValue()) {
					countD++;
			} else {
				var divDate  = stockInfo.divDate;
				var divValue = stockInfo.divValue;

				var myValue = divList.stream().filter(o -> o.date.equals(divDate)).map(o -> o.value).findFirst().orElse(null);
				if (myValue == null) {
					logger.error("Unexpected divDate");
					logger.error("  {}  {}  {}  {}", isinCode, stockCode, divDate, name);
					throw new UnexpectedException("Unexpected divDate");
				}

				if (myValue.compareTo(BigDecimal.ZERO) == 0) {
					countE++;
				} else if (divValue.compareTo(myValue) == 0) {
					countF++;
				} else {
					countG++;
					// modify divList with factor
					var factor = divValue.divide(myValue, 3, RoundingMode.HALF_EVEN);
					logger.info("XX  {}  {}  {}  {}", fundInfo.isinCode, fundInfo.stockCode, factor.toPlainString(), fundInfo.name);
					// modify divList with factor
					for(var e: divList) {
						e.value = e.value.multiply(factor);
					}
				}
			}

			StorageFundJP.FundDiv.save(fundInfo.isinCode, divList);
		}

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
		logger.info("countE  {}", countE);
		logger.info("countF  {}", countF);
		logger.info("countG  {}", countG);

		StorageFundJP.FundDiv.touch();

	}


	private boolean allZero(List<DailyValue> list) {
		for(var e: list) {
			if (e.value.compareTo(BigDecimal.ZERO) != 0) {
				return false;
			}
		}
		return true;
	}

}
