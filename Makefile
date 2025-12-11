#
#
#

DATA_PATH      := $(shell jq <data/storage.json -r '.path')
FINANCE_PATH   := $(DATA_PATH)finance


.PHONY: all build check-finance-path


all: check-finance-path
	@echo "DATA_PATH                 $(DATA_PATH)"
	@echo "FINANCE_PATH              $(FINANCE_PATH)"

check-finance-path:
#	@echo "DATA_PATH_FILE  !$(DATA_PATH_FILE)!"
#	@echo "DATA_PATH       !$(DATA_PATH)!"
	@if [ ! -d $(FINANCE_PATH) ]; then \
		echo "FINANCE_PATH  no directory  !$(FINANCE_PATH)!" ; \
		exit 1 ; \
	fi

update-crontab:
	crontab data/crontab


check-temp-file: check-finance-path
	find .. $(FINANCE_PATH) -regex '.*/\.DS.*'           -print
	find .. $(FINANCE_PATH) -regex '.*/\.[^a-zA-Z0-9].*' -print


#
# common task
#

log-start:
	@date +'%F %T LOG START $(LOG_TITLE)'

log-stop:
	@date +'%F %T LOG STOP  $(LOG_TITLE)'

log-time:
	@date +'%F %T LOG TIME'

# maintenance of save and log

#
# finance/save
#
maintain-save-file: check-finance-path
	find $(FINANCE_PATH)/save   -mtime +7d -print -delete
	find $(FINANCE_PATH)/report -mtime +7d -print -delete

maintain-log-file: check-finance-path
	@date +'%F %T TAR START'
	tar cfz $(FINANCE_PATH)/save/log_$$(date +%Y%m%d).taz tmp/*.log
	@date +'%F %T TAR STOP'
	echo -n >tmp/yokwe-finance.log
	echo -n >tmp/cron.log

save-all: check-finance-path save-data rsync-to-Backup2T

save-data: check-finance-path
	@date +'%F %T TAR START'
	cd $(FINANCE_PATH); tar cfz save/data_$$(date +%Y%m%d).taz    data
	@date +'%F %T TAR STOP'

rsync-to-Backup2T:
	@date +'%F %T RSYNC START'
	rsync -ah --delete /Volumes/SanDisk2T/* /Volumes/Backup2T/
	@date +'%F %T RSYNC STOP'


#
# tools
#
generate-makefile:
	ant generate-makefile

generate-dot:
	ant generate-dot
	dot -Kdot -Tpdf tmp/dot/a.dot >tmp/dot/a.pdf

tmp/dot/a.pdf: tmp/dot/a.dot
	dot -Kdot -Tpdf tmp/dot/a.dot >tmp/dot/a.pdf


#
# build full-build
#
build:
	( cd ../yokwe-util; make build )
	mvn ant:ant install
	
full-build:
	( cd ../yokwe-util; make full-build )
	mvn clean ant:ant install

#
# primary targets
#
update-all:
	make -f tmp/update-all.make update-all

update-all-debug:
	make -f tmp/update-all.make -n update-all


update-data-jp: update-jpx update-jita update-rakuten-jp update-smtb-jp update-sony-jp
	make update-all

update-data-us: update-rakuten-us update-us-exchange
	make update-all


update-fx:
	ant mizuho-update-fx-rate


#
# secondary targets
#

update-jpx:
	ant jpx-update-stock-list jpx-update-etf jpx-update-etn jpx-update-infra jpx-update-reit

update-jita:
	ant jita-update-fund-info

update-us-exchange:
	ant bats-update-stock-code-name nasdaq-update-stock-code-name nyse-update-stock-code-name

update-rakuten-jp:
	ant rakuten-update-trading-fund-jp

update-rakuten-us:
	ant rakuten-update-trading-stock-us

update-sony-jp:
	ant sony-update-trading-fund-jp

update-smtb-jp:
	ant smtb-update-trading-fund-jp

update-mizuho:
	ant mizuho-update-fx-rate
