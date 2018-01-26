package com.modbus.servlets;

import models.RowModel;
import parser.ModbusDataParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@WebServlet("/parsedData")
public class Servlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ModbusDataParser modbusDataParser = new ModbusDataParser();
        List<RowModel> results = new ArrayList<>();
        modbusDataParser.loadFromFile("http://tuftuf.gambitlabs.fi/feed.txt", ModbusDataParser.FileLocalization.ONLINE);//http://tuftuf.gambitlabs.fi/feed.txt//file.txt
        modbusDataParser.loadInstructions("/Users/maciej_bukowski/ModbusDataParser/src/main/webapp/WEB-INF/instruction.txt");
        results = modbusDataParser.processData();
        request.setAttribute("results", results);
        request.getRequestDispatcher("/WEB-INF/views/parsedData.jsp").forward(request, response);
    }
}
