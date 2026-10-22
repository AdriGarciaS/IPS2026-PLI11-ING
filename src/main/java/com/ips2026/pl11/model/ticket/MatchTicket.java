package com.ips2026.pl11.model.ticket;

public class MatchTicket {

	private final int id;
    private final int matchId;
    private final String grandstand; // 'A', 'B', 'C', 'D'
    private final String section;    // 'A'..'F'
    private final int rowNum;        // 1..10
    private final int seatNum;       // 1..15
    private final double price;      // 30.0

    public MatchTicket(int id, int matchId, String grandstand, String section, int rowNum, int seatNum, double price) {
        this.id = id;
        this.matchId = matchId;
        this.grandstand = grandstand;
        this.section = section;
        this.rowNum = rowNum;
        this.seatNum = seatNum;
        this.price = price;
    }

    public int getId() { 
    	return id; 
    }
    
    public int getMatchId() { 
    	return matchId; 
    }
    
    public String getGrandstand() { 
    	return grandstand; 
    }
    
    public String getSection() { 
    	return section; 
    }
    
    public int getRowNum() { 
    	return rowNum; 
    }
    
    public int getSeatNum() { 
    	return seatNum; 
    }
    
    public double getPrice() { 
    	return price; 
    }
}
