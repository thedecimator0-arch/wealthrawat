package com.bank.dao;

import com.bank.model.Customer;
import com.bank.util.DatabaseUtil;

import java.sql.*;

public class CustomerDAO {
    public Customer findById(String customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                        rs.getString("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone")
                );
            }
        }
        return null;
    }
}
