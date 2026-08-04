package financa.backend.Model;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Acoes {
    @SerializedName(value = "stock", alternate = {"symbol"})
    private String stock;
    @SerializedName(value = "name", alternate = {"shortName"})
    private String name;
    @SerializedName(value = "close", alternate = {"regularMarketPrice"})
    private Double close;
    @SerializedName(value = "change", alternate = {"regularMarketChangePercent"})
    private Double change;
    @SerializedName(value = "logo", alternate = {"logourl"})
    private String logo;
    private String type;
    private String longName;
    private String currency;
    private Double regularMarketChange;
    private String regularMarketTime;
    private Double regularMarketDayHigh;
    private Double regularMarketDayLow;
    private Long regularMarketVolume;
    private Double regularMarketPreviousClose;
    private Double regularMarketOpen;
    private String regularMarketDayRange;
    private Double fiftyTwoWeekLow;
    private Double fiftyTwoWeekHigh;
    private String fiftyTwoWeekRange;
    private Double marketCap;
    private Double priceEarnings;
    private Double earningsPerShare;
}
