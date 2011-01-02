package grails.plugin.multitenant.core.util.log.log;

import grails.plugin.multitenant.core.util.TenantUtils;
import org.apache.log4j.MDC;
import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Class for parsing patterns from log formats and injecting the current tenant's id.  The pattern for
 * displaying the current tenantId is %T
 */
public class MultiTenantPatternParser extends PatternParser
{

    public static final char TENANT_CONVERTER_CHAR = 'T';

    public MultiTenantPatternParser(String pattern)
    {
        super(pattern);
    }

    public void finalizeConverter(char c)
    {
        if (c == TENANT_CONVERTER_CHAR)
        {
            addConverter(new MultiTenantPatternConverter(formattingInfo));
            currentLiteral.setLength(0);
        } else
        {
            super.finalizeConverter(c);
        }
    }

    /**
     * This class will actually return the replacement string for the logger (just the integer of the current tenant)
     */
    private class MultiTenantPatternConverter extends PatternConverter
    {

        private static final String MDC_KEY = "VISITED_BY_MULTI_TENANT";

        MultiTenantPatternConverter(FormattingInfo formattingInfo)
        {
            super(formattingInfo);
        }

        public String convert(LoggingEvent event)
        {
            boolean mdcAdded = false;
            try {
                Object mdcValue = MDC.get(MDC_KEY);
                if (mdcValue==null) {
                    MDC.put(MDC_KEY, Boolean.TRUE);
                    mdcAdded = true;
                    final String rtn;
                    rtn = TenantUtils.getCurrentTenantName().toString();
                    return rtn;
                } else {
                    return "n/a";
                }
            } finally {
                if (mdcAdded) {
                    MDC.remove(MDC_KEY);
                }
            }
        }
    }
}
