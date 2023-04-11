package racingcar.controller;

import racingcar.domain.*;
import racingcar.dto.RacingCarDto;
import racingcar.utils.Parser;
import racingcar.validator.Validator;
import racingcar.view.InputView;
import racingcar.view.OutputView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RacingConsoleController {
    private static final int FIRST_CAR_INDEX = 0;
    private final OutputView outputView = new OutputView();
    private final InputView inputView = new InputView();
    private final Validator validator = new Validator();

    public void run() {
        List<String> carNames = getValidCarNames();
        int tryCount = getValidTryCount();

        RoundManager roundManager = initializeRoundManager(carNames);

        outputView.printResultHeader();
        outputView.printRoundResult(roundManager.getStartStatus());
        for (int roundCount = 0; roundCount < tryCount; roundCount++) {
            roundManager.runRound();
            outputView.printRoundResult(roundManager.getStatus());
        }

        List<String> winningCarsName = getWinningCarsName(roundManager.getSortedRacingCars());
        outputView.printWinners(winningCarsName);
    }

    private List<String> getValidCarNames() {
        try {
            String carNames = inputView.readCarName();
            List<String> parsedCarNames = Parser.parsing(carNames, ",");
            validator.validateNames(parsedCarNames);
            return parsedCarNames;
        } catch (IllegalArgumentException exception) {
            outputView.printErrorMessage(exception.getMessage());
            return getValidCarNames();
        }
    }

    private int getValidTryCount() {
        try {
            String tryCount = inputView.readTryCount();
            validator.validateTryCount(tryCount);
            return Integer.parseInt(tryCount);
        } catch (IllegalArgumentException exception) {
            outputView.printErrorMessage(exception.getMessage());
            return getValidTryCount();
        }
    }


    private void setCars(List<String> carNames, RoundManager roundManager) {
        for (String carName : carNames) {
            roundManager.addRacingCar(new RacingCar(carName));
        }
    }

    private RoundManager initializeRoundManager(List<String> carNames) {
        Range range = new Range(4, 9);
        NumberGenerator numberGenerator = new RandomNumberGenerator();
        AdvanceJudgement advanceJudgement = new AdvanceJudgement(range, numberGenerator);
        RoundManager roundManager = new RoundManager(advanceJudgement);
        setCars(carNames, roundManager);
        return roundManager;
    }

    private List<String> getWinningCarsName(List<RacingCarDto> sortedRacingCars) {
        RacingCarDto firstCar = sortedRacingCars.get(FIRST_CAR_INDEX);
        return sortedRacingCars.stream()
                .filter(car -> car.getPosition().equals(firstCar.getPosition()))
                .map(RacingCarDto::getName)
                .collect(Collectors.toList());
    }
}