package com.gbt.cdms.amos.reader;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.gbt.cdms.amos.constant.DatabaseQueries;
import com.gbt.cdms.amos.model.Gisdrmstp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FetchGisdrmstpReader extends JdbcCursorItemReader<Gisdrmstp> implements ItemReader<Gisdrmstp> {

    public FetchGisdrmstpReader(@Autowired DataSource dataSource) {
        setDataSource(dataSource);
        setSql(DatabaseQueries.FETCH_GISDRMSTP);
        setVerifyCursorPosition(false);
        setRowMapper(new RowMapper<Gisdrmstp>() {
            @Override
            public Gisdrmstp mapRow(ResultSet rs, int rowNum) throws SQLException {
                log.info("FetchGisdrmstpReader-Fetching data from GISDRMSTP table for account Number {}", rs.getString("dracct"));
                Gisdrmstp dks2Model = new Gisdrmstp();
                dks2Model.setDrmcid(rs.getString("drmcid"));
                dks2Model.setDracct(rs.getString("dracct"));
                dks2Model.setDrrgc(rs.getString("drrgc"));
                dks2Model.setDrdtup(rs.getInt("drdtup"));
                dks2Model.setDrtmup(rs.getInt("drtmup"));
                dks2Model.setDrpgup(rs.getString("drpgup"));
                dks2Model.setDrusup(rs.getString("drusup"));
                return dks2Model;
            }
        });
    }
}