package yokwe.finance.data.provider.mizuho;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.finance.data.type.FXRate;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.makefile.Makefile;
import yokwe.util.update.UpdateList;

public class UpdateFXRate extends UpdateList<Quote> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Makefile MAKEFILE = Makefile.builder().
		input().
		output(StorageMizuho.FXRate).
		build();
		
	public static void main(String[] args) {
		callUpdate();
	}
	
	@Override
	protected List<Quote> downloadFile() {
		logger.info("EPOCH DATE  {}", EPOCH_DATE);
		
		String string = HttpUtil.getInstance().withCharset(ENCODING_CSV).downloadString(URL_CSV);
		StorageMizuho.Quote.write(string);
		
		Reader reader = new StringReader(string);
		return CSVUtil.read(Quote.class).withHeader(false).file(reader);
	}
	private static final LocalDate EPOCH_DATE   = LocalDate.of(2024, 1, 1);
	private static final String    ENCODING_CSV = "SHIFT_JIS";
	private static final String    URL_CSV      = "https://www.mizuhobank.co.jp/market/quote.csv";

	@Override
	protected void updateFile(List<Quote> list) {
		// sanity check
		{
			// line 0
			{
				Quote quote = list.get(0);
				if (!quote.DATE.isEmpty()) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
				// 参考相場
				if (!quote.TWD.equals("参考相場")) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
			}
			// line 1
			{
				Quote quote = list.get(1);
				if (!quote.DATE.isEmpty()) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
				// 米ドル
				if (!quote.USD.equals("米ドル")) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
			}
			// line 2
			{
				Quote quote = list.get(2);
				if (!quote.DATE.isEmpty()) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
				if (!quote.USD.equals("USD")) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
			}
			// line 3
			{
				Quote quote = list.get(3);
				if (!quote.DATE.equals("2002/4/1")) {
					logger.error("Unexpected");
					throw new UnexpectedException("Unexpected");
				}
			}
		}
		
		List<FXRate> result = new ArrayList<>();
		for(int i = 3; i < list.size(); i++) {
			Quote value = list.get(i);
			
			var date = toLocalDate(value.DATE);
			var usd  = new BigDecimal(value.USD).setScale(2);
			
			result.add(new FXRate(date, usd));
		}
		
		// remove entry before EPOC_DATE
		result.removeIf(o -> o.date.isBefore(EPOCH_DATE));
		
		logger.info("date  {} - {}", result.getFirst().date, result.getLast().date);
		
		save(result, StorageMizuho.FXRate); // use save for make
	}
	private LocalDate toLocalDate(String string) {
		Matcher m = PAT_YYYYMMDD.matcher(string);
		
		if (m.matches() && m.groupCount() == 3) {
			int yyyy = Integer.parseInt(m.group("yyyy"));
			int mm   = Integer.parseInt(m.group("mm"));
			int dd   = Integer.parseInt(m.group("dd"));
			
			return LocalDate.of(yyyy, mm, dd);
		} else {
			logger.error("Unexpected string");
			logger.error("  string {}", string);
			throw new UnexpectedException("Unexpected string");
		}
	}
	private static final Pattern PAT_YYYYMMDD = Pattern.compile("^(?<yyyy>20[0-9]{2})/(?<mm>[01]?[0-9])/(?<dd>[0-3]?[0-9])$");
}