package com.theironyard.charlotte.controllers;

import com.theironyard.charlotte.entities.Team;
import com.theironyard.charlotte.services.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by mfahrner on 9/16/16.
 */@Controller
public class FootballController {

    @Autowired
    TeamRepository teams;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(Model model) {
        List<Team> teamList;
        teamList = (List)teams.findAll();


        model.addAttribute("teams", teamList);
        return "home";
    }


    @RequestMapping(path = "/", method = RequestMethod.POST)
    public String generateProb(Model model, int homeId, int awayId) {

        List<Team> teamList;
        teamList = (List)teams.findAll();

        Team homeTeam = teams.findOne(homeId);
        Team awayTeam = teams.findOne(awayId);
        Team averageTeam = teams.findOne(33);

        double homeGWPLogit = homeTeam.computeGWPLogit(homeTeam.getTeamLogit(), averageTeam.getTeamLogit());
        double awayGWPLogit = awayTeam.computeGWPLogit(awayTeam.getTeamLogit(), averageTeam.getTeamLogit());

        double homeOppAveGWPLogit = computeOppGWPLogit(homeTeam.getOpponents());
        double awayOppAveGWPLogit = computeOppGWPLogit(awayTeam.getOpponents());

        double homeAdjustedGWPLogit = homeTeam.computeAdjustedGWPLogit(homeTeam.getTeamLogit(),
                averageTeam.getTeamLogit(), homeOppAveGWPLogit);
        double awayAdjustedGWPLogit = awayTeam.computeAdjustedGWPLogit(awayTeam.getTeamLogit(),
                averageTeam.getTeamLogit(), awayOppAveGWPLogit);

        double ultimateHomeLogit = homeTeam.computeFinalLogit(homeAdjustedGWPLogit, homeOppAveGWPLogit,
                awayAdjustedGWPLogit, awayOppAveGWPLogit);

        double homeTeamOdds = homeTeam.computeOdds(ultimateHomeLogit);

        double homeTeamProbWin = homeTeam.computeProbability(homeTeamOdds);

        double awayTeamProbWin = 1 - homeTeamProbWin;

        model.addAttribute("homeTeamWin", homeTeamProbWin);
        model.addAttribute("awayTeamWin", awayTeamProbWin);
        model.addAttribute("teams", teamList);
        return "homePlus";
    }

    @PostConstruct
    public void init() throws IOException {
        if (teams.count() == 0) {
            File f = new File("stats.csv");
            Scanner fileScanner = new Scanner(f);

            while (fileScanner.hasNext()) {

                String line = fileScanner.nextLine();
                String[] columns = line.split(",");
                Integer some = Integer.valueOf(columns[8]);

                Team team = new Team();

                ArrayList<Integer> oppList = new ArrayList<>();
                oppList.add(some);
                oppList.add(Integer.valueOf(columns[9]));

                double teamLogit = team.computeTeamLogit(Double.valueOf(columns[1]), Double.valueOf(columns[2]),
                        Double.valueOf(columns[5]), Double.valueOf(columns[6]), Double.valueOf(columns[3]),
                        Double.valueOf(columns[4]), Double.valueOf(columns[7]));

                team = new Team(columns[0], Double.valueOf(columns[1]), Double.valueOf(columns[2]),
                        Double.valueOf(columns[5]), Double.valueOf(columns[6]), Double.valueOf(columns[3]),
                        Double.valueOf(columns[4]), Double.valueOf(columns[7]), teamLogit, oppList);

                teams.save(team);
            }
        }
    }

    public double computeOppGWPLogit(ArrayList<Integer> oppList) {
        double totalOppGWP = 0;
        double aveLogit = teams.findOne(33).getTeamLogit();
        for (int i = 0; i < oppList.size(); i++) {
            // finds opponent team
            Team oppTeam = teams.findOne(oppList.get(i));
            // plug into formula for gwpLogit
            // plug average into formula id 33


            totalOppGWP = oppTeam.computeGWPLogit(oppTeam.getTeamLogit(), aveLogit) + totalOppGWP;
        }
        return totalOppGWP/oppList.size();

    }

}
