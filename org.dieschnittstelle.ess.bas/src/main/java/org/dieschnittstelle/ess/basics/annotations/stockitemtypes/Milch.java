package org.dieschnittstelle.ess.basics.annotations.stockitemtypes;

import org.dieschnittstelle.ess.basics.annotations.*;

@StockItem
public class Milch {

    @Units
    private int menge;

    @DisplayAs("brandName:Field:Make, it:confusing, ")
    @Brandname
    private String markenname;

    @DisplayAs("amount")
    public int getMenge() {
        return menge;
    }

    public void setMenge(int menge) {
        this.menge = menge;
    }

    @DisplayAs("brandName")
    public String getMarkenname() {
        return markenname;
    }

    public void setMarkenname(String markenname) {
        this.markenname = markenname;
    }

    @Initialise
    public void lagern(int units, String brandname) {
        this.markenname = brandname;
        this.menge = units;
    }

    /**
     * our own toString method
     */
    public String toString() {
        return "<Milch " + this.markenname + " " + this.menge + ">";
    }

}
