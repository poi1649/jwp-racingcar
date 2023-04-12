package racingcar.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import racingcar.dao.CarDao;
import racingcar.dao.GameDao;
import racingcar.domain.AdvanceJudgement;
import racingcar.domain.GameResult;
import racingcar.domain.NumberGenerator;
import racingcar.domain.RacingCar;
import racingcar.domain.RacingGame;
import racingcar.domain.RandomNumberGenerator;
import racingcar.domain.Range;
import racingcar.dto.RacingCarDto;
import racingcar.dto.RacingCarResultDto;

@Service
public class RacingGameService {
    public static final int MOVABLE_MIN_THRESHOLD = 4;
    public static final int MOVABLE_MAX_THRESHOLD = 9;

    private final GameDao gameDao;
    private final CarDao carDao;

    public RacingGameService(GameDao gameDao, CarDao carDao) {
        this.gameDao = gameDao;
        this.carDao = carDao;
    }

    public long run(List<String> carNames, int count) {
        RacingGame racingGame = initializeGame(carNames);

        for (int i = 0; i < count; i++) {
            racingGame.runRound();
        }

        long gameId = gameDao.save(count);

        Map<RacingCar, GameResult> results = racingGame.getResult();
        results.forEach((racingCar, isWin) ->
                carDao.save(RacingCarResultDto.of(racingCar, isWin.getValue(), gameId)));

        return gameId;
    }

    private RacingGame initializeGame(List<String> carNames) {
        Range range = new Range(MOVABLE_MIN_THRESHOLD, MOVABLE_MAX_THRESHOLD);
        NumberGenerator numberGenerator = new RandomNumberGenerator();
        AdvanceJudgement advanceJudgement = new AdvanceJudgement(range, numberGenerator);
        List<RacingCar> racingCars = carNames.stream().map(RacingCar::new).collect(Collectors.toUnmodifiableList());
        return new RacingGame(racingCars, advanceJudgement);
    }

    public List<String> findWinnersById(long id) {
        return carDao.findWinnersById(id);
    }

    public List<RacingCarDto> findCarsById(long id) {
        return carDao.findCarsById(id);
    }
}
