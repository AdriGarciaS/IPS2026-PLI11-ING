package com.ips2026.pl11.controller.shareholder;

import com.ips2026.pl11.data.shareholder.MarketPriceDAO;
import com.ips2026.pl11.data.shareholder.ShareHoldingDAO;
import com.ips2026.pl11.model.shareholder.MarketPrice;
import com.ips2026.pl11.model.shareholder.Shareholder;
import com.ips2026.pl11.model.shareholder.SharesWallet;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller of the shares wallet: builds the wallet of a shareholder with
 * the latest market price.
 */
public class SharesWalletController {

    private final ShareHoldingDAO holdingDAO;
    private final MarketPriceDAO priceDAO;

    public SharesWalletController(ShareHoldingDAO holdingDAO, MarketPriceDAO priceDAO) {
        this.holdingDAO = holdingDAO;
        this.priceDAO = priceDAO;
    }

    /**
     * The wallet of the shareholder, valued with the latest market price.
     *
     * @throws IllegalStateException if there is no market price at all
     */
    public SharesWallet loadWallet(Shareholder shareholder) throws SQLException {
        List<MarketPrice> latest = priceDAO.findLatest(2);
        if (latest.isEmpty()) {
            throw new IllegalStateException("There is no market price for the shares yet.");
        }
        MarketPrice current = latest.get(0);
        MarketPrice previous = latest.size() > 1 ? latest.get(1) : null;
        return new SharesWallet(shareholder, holdingDAO.findByShareholder(shareholder.getId()), current, previous);
    }
}
