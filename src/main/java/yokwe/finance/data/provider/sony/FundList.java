package yokwe.finance.data.provider.sony;

import yokwe.util.ToString;
import yokwe.util.json.JSON.Optional;

public class FundList {
// Request URL
	// https://www.wam.abic.co.jp/ap03/services/cafndlst/getCAFndLst?
	// _com_id_product=1&
	// _com_id_company=C160035&
	// _com_id_screen=10107101&
	// _biz_id_abiccode=&
	// id_companyapp=003&
	// _com_id_session=02202512050269110315&
	// d_keyword=&
	// id_fundlist=&
	// fg_stop=&
	// id_companylclass=&
	// callback=callback_ca_fndlst&
	// _1764897548208=
	
    public static class ns_companylclass {
        public String id_companylclass;
        public String st_companylclass;

        public ns_companylclass() {
        }

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    public static class ns_allfees {
        @Optional public String id_slidefee;
        @Optional public String id_tradingmethod;
        @Optional public String max_slidefee;
        @Optional public String max_slidefee_ex;
        @Optional public String min_slidefee;
        @Optional public String min_slidefee_ex;
        @Optional public String ra_sellfee;
        @Optional public String ra_sellfee_ex;
        @Optional public String st_tradingmethod;

        public ns_allfees() {
        }

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    public static class ns_badges {

        public ns_badges() {
        }

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    public static class ns_docmentllink {

        public ns_docmentllink() {
        }

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    public static class ns_prospectusllink {

        public ns_prospectusllink() {
        }

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    public static class ns_tradingmethod {

        public ns_tradingmethod() {
        }

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    public static class ob_rankinginfo {
        @Optional public String no_01_1;
        @Optional public String no_01_2;
        @Optional public String no_01_3;
        @Optional public String no_21_4;
        @Optional public String no_21_6;
        @Optional public String no_21_7;
        @Optional public String no_24_1;
        @Optional public String no_25_1;

        public ob_rankinginfo() {
        }

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    public static class ns_favourregisterfund {
        public String dt_dividend;
        public String dt_docvalidatedate;
        public String dt_from;
        public String dt_settlingday;
        public String dt_to;
        public String dt_yyyymmdd;
        public String fg_buy_button;
        public String fg_comparable;
        public String fg_growth_targetcategory;
        public String fg_highrisk;
        public String fg_initialoffer;
        public String fg_mmf_mrf;
        public String fg_nearrepay;
        public String fg_newfund;
        public String fg_nisa_targetcategory;
        public String fg_nobasicinfo;
        public String fg_regularsavingplan;
        public String fg_stop;
        public String id_abicfund;
        public String id_companylclass;
        public String id_fundlist;
        public String id_fundtype;
        public String id_rating;
        public String no_companydisplay;
        public ns_allfees[] ns_allfees;
        public ns_badges[] ns_badges;
        public ns_docmentllink[] ns_docmentllink;
        public ns_prospectusllink[] ns_prospectusllink;
        public ns_tradingmethod[] ns_tradingmethod;
        public ob_rankinginfo ob_rankinginfo;
        public String qu_settlecnt;
        public String ra_aveprofit1year;
        public String ra_aveprofit3year;
        public String ra_aveprofit5year;
        public String ra_change;
        public String ra_change_org;
        public String ra_conversionyield_year;
        public String ra_max_fee;
        public String ra_profit1mon;
        public String ra_profit1year;
        public String ra_profit3mon;
        public String ra_profit3year;
        public String ra_profit5year;
        public String ra_profit6mon;
        public String ra_profits;
        public String ra_reward;
        public String ra_reward_sort;
        public String ra_stdev1year;
        public String ra_stdev3year;
        public String ra_stdev5year;
        public String sm_annualdistrib;
        public String sm_basic;
        public String sm_change;
        public String sm_distrib;
        public String sm_lastdistrib;
        public String sm_netfund;
        public String sm_netfund_m;
        public String sm_reservation;
        public String st_companylclass;
        public String st_currencyabbr;
        public String st_fundshortname1;
        public String st_fundshortname2;
        public String st_htmlcolor;
        public String st_itcompany;
        public String st_rating;
        public String st_url;
        public String st_yyyymmdd;

        public ns_favourregisterfund() {
        }

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

    public static class Data {
        public String err_msg;
        public String err_status;
        public String fg_availabletab;
        public String fg_foreignfnd_simpledsp;
        public String fg_freeword_search;
        public String fg_salesstoptab;
        public String id_session;
        public ns_companylclass[] ns_companylclass;
        public ns_favourregisterfund[] ns_favourregisterfund;
        public String st_availabletab;
        public String st_footermessage1;
        public String st_footermessage2;
        public String st_footermessage3;
        public String st_fundingnisa;
        public String st_growthinvestment;
        public String st_headermessage1;
        public String st_headermessage2;
        public String st_headermessage3;
        public String st_yyyymmdd;

        public Data() {
        }

        @Override
        public String toString() {
            return ToString.withFieldName(this);
        }
    }

}
