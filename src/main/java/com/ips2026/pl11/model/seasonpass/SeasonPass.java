package com.ips2026.pl11.model.seasonpass;


/**
 * Domain entity representing a Fan's Season Pass for a specific team.
 */
public class SeasonPass {

    private int id;
    private int fanId;
    private int teamId;
    private String grandstand;
    private String section;
    private int rowNum;
    private int seatNum;
    private String ageCategory;
    private double price;
    private String startDate;
    private String endDate;

    public SeasonPass(int fanId, int teamId, String grandstand, String section, int rowNum, int seatNum, String ageCategory, double price, String startDate, String endDate) {
        this.fanId = fanId;
        this.teamId = teamId;
        this.grandstand = grandstand;
        this.section = section;
        this.rowNum = rowNum;
        this.seatNum = seatNum;
        this.ageCategory = ageCategory;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public int getId() { 
    	return id; 
    }
    
    public void setId(int id) { 
    	this.id = id; 
    }
    
    public int getFanId() { 
    	return fanId; 
    }
    
    public int getTeamId() { 
    	return teamId; 
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
    
    public String getAgeCategory() { 
    	return ageCategory; 
    }
    
    public double getPrice() { 
    	return price; 
    }
    
    public String getStartDate() { 
    	return startDate; 
    }
    
    public String getEndDate() { 
    	return endDate;
    }
}