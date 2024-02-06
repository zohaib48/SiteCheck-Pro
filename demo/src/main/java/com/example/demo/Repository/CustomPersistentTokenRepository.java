package com.example.demo.Repository;


import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Date;

@Service
public class CustomPersistentTokenRepository extends JdbcTokenRepositoryImpl {

   
    public CustomPersistentTokenRepository(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    @Transactional
    public void createNewToken(PersistentRememberMeToken token) {
        super.createNewToken(token);
    }

    @Override
    @Transactional
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        super.updateToken(series, tokenValue, lastUsed);
    }

    @Override
    @Transactional
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        return super.getTokenForSeries(seriesId);
    }

    @Override
    @Transactional
    public void removeUserTokens(String username) {
        super.removeUserTokens(username);
    }
}
