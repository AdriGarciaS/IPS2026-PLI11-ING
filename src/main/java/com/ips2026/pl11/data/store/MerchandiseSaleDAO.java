package com.ips2026.pl11.data.store;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.store.CartLine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * SQL operations on the MERCHANDISING_SALE table.
 */
public class MerchandiseSaleDAO {

    /**
     * Registers a purchase: inserts one MERCHANDISING_SALE row per cart line
     * (all of them with the same new sale_number) and subtracts the sold
     * units from the stock of each product.
     *
     * <p>Everything runs in a single transaction: if any product does not
     * have enough available units, nothing is stored.</p>
     *
     * @return the sale_number assigned to the purchase
     */
    public int registerSale(List<CartLine> lines, LocalDate date) throws SQLException {
        String sqlNextNumber = "SELECT COALESCE(MAX(sale_number), 0) + 1 FROM MERCHANDISING_SALE";
        String sqlUpdateStock = "UPDATE MERCHANDISING SET available_units = available_units - ? "
                + "WHERE id = ? AND available_units >= ?";
        String sqlInsertSale = "INSERT INTO MERCHANDISING_SALE "
                + "(sale_number, merch_id, units_sold, total_price, sale_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConexionBD.obtenerConexion()) {
            connection.setAutoCommit(false);
            try (Statement nextNumber = connection.createStatement();
                 PreparedStatement updateStock = connection.prepareStatement(sqlUpdateStock);
                 PreparedStatement insertSale = connection.prepareStatement(sqlInsertSale)) {

                int saleNumber;
                try (ResultSet result = nextNumber.executeQuery(sqlNextNumber)) {
                    result.next();
                    saleNumber = result.getInt(1);
                }

                for (CartLine line : lines) {
                    int merchId = line.getMerchandise().getId();

                    // Only subtracts the stock if there are enough units left.
                    updateStock.setInt(1, line.getUnits());
                    updateStock.setInt(2, merchId);
                    updateStock.setInt(3, line.getUnits());
                    if (updateStock.executeUpdate() == 0) {
                        throw new SQLException("There are not enough available units of '"
                                + line.getMerchandise().getName() + "'");
                    }

                    insertSale.setInt(1, saleNumber);
                    insertSale.setInt(2, merchId);
                    insertSale.setInt(3, line.getUnits());
                    insertSale.setDouble(4, line.getSubtotal().doubleValue());
                    insertSale.setString(5, date.toString()); // ISO format: YYYY-MM-DD
                    insertSale.executeUpdate();
                }

                connection.commit();
                return saleNumber;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}
