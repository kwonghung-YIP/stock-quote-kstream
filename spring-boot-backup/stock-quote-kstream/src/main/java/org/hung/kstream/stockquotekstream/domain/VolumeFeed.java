package org.hung.kstream.stockquotekstream.domain;

import lombok.Data;

@Data
public class VolumeFeed extends QuoteFeed {

    private long volume;
}
