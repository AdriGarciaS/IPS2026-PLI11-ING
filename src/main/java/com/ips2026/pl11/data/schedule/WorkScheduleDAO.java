package com.ips2026.pl11.data.schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.schedule.WorkSchedule;

public class WorkScheduleDAO {

    public List<WorkSchedule> getAllSchedules() throws SQLException {
        String sql = "SELECT id, employee_id, week_day, start_time, end_time FROM work_schedule ORDER BY id";
        List<WorkSchedule> workSchedules = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
            Statement sentencia = conexion.createStatement();
            ResultSet resultado = sentencia.executeQuery(sql)) {

            while (resultado.next()) {
                workSchedules.add(new WorkSchedule(resultado.getLong("id"), resultado.getLong("employee_id"),
                    resultado.getInt("week_day"), LocalTime.parse(resultado.getString("start_time")),
                    LocalTime.parse(resultado.getString("end_time"))));
            }
        }

        return workSchedules;
    }

    public List<WorkSchedule> getSchedulesByEmployee(long employeeId) throws SQLException {
        String sql = "select id, employee_id, week_day, start_time, end_time "
            + "from work_schedule where employee_id = ? order by week_day, start_time";

        List<WorkSchedule> result = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setLong(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new WorkSchedule(rs.getLong("id"), rs.getLong("employee_id"), rs.getInt("week_day"),
                        LocalTime.parse(rs.getString("start_time")), LocalTime.parse(rs.getString("end_time"))));
                }
            }
        }

        return result;
    }

    public void insertSchedule(WorkSchedule ws) throws SQLException {
        String sql = "insert into work_schedule (employee_id, week_day, start_time, end_time) values (?, ?, ?, ?)";
        
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(sql)) {
            
            ps.setLong(1, ws.getEmployeeId());
            ps.setInt(2, ws.getWeekDay());
            ps.setString(3, String.valueOf(ws.getStartTime()));
            ps.setString(4, String.valueOf(ws.getEndTime()));
            
            ps.executeUpdate();
        }
    }
    
    public WorkSchedule getScheduleById(long id) throws SQLException{
        String sql = "select id, employee_id, week_day, start_time, end_time "
            + "from work_schedule where id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    return new WorkSchedule(
                        rs.getLong("id"),
                        rs.getLong("employee_id"),
                        rs.getInt("week_day"),
                        LocalTime.parse(rs.getString("start_time")),
                        LocalTime.parse(rs.getString("end_time"))
                    );
                }
            }
        }

        return null;
    }
}
