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
import com.gbt.cdms.amos.model.Giscxmstp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FetchGiscxmstpReader extends JdbcCursorItemReader<Giscxmstp> implements ItemReader<Giscxmstp> {

    public FetchGiscxmstpReader(@Autowired DataSource dataSource) {
        setDataSource(dataSource);
        setSql(DatabaseQueries.FETCH_GISCXMSTP);
        setVerifyCursorPosition(false);
        setRowMapper(new RowMapper<Giscxmstp>() {
            @Override
            public Giscxmstp mapRow(ResultSet rs, int rowNum) throws SQLException {
                log.info("FetchGiscxmstpReader-Fetching data from GISCXMSTP table for account Number - {}",
                        rs.getString("cxacct"));
                Giscxmstp giscxmstp = new Giscxmstp();
                giscxmstp.setCxacct(rs.getString("cxacct"));
                giscxmstp.setCxacbr(rs.getString("cxacbr"));
                giscxmstp.setCxmcid(rs.getString("cxmcid"));
                return giscxmstp;
            }
        });
    }
}