package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.init();
        Scanner sharedScanner = new Scanner(System.in);
        UserDao sharedUserDao = new UserDao();
        new Authentication(sharedScanner, sharedUserDao).start();
    }
}