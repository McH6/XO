package com.mch.xoServer.datasource;

import com.mch.xoServer.model.Game;

public interface GameDataSource {

    void saveGame(Game game);

    Game loadGame(String player1, String player2);

}
