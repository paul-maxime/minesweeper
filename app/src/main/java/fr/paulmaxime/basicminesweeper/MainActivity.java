package fr.paulmaxime.basicminesweeper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private GameView game_view;
    private Button marking_mode_button;
    private TextView marked_mines_text;
    private TextView total_mines_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        game_view = (GameView)findViewById(R.id.game_view);
        marking_mode_button = (Button)findViewById(R.id.marking_mode_button);
        marked_mines_text = (TextView)findViewById(R.id.marked_mines_text);
        total_mines_text = (TextView)findViewById(R.id.total_mines_text);

        game_view.setUpdateListener(new GameUpdateListener() {
            @Override
            public void onGameUpdated() {
                updateGameInformation();
            }
        });
        updateGameInformation();
    }

    public void resetGameButtonClick(View view) {
        game_view.resetGame();
        game_view.invalidate();
    }

    public void markingModeButtonClick(View view) {
        game_view.toggleMarkingMode();
        marking_mode_button.setText(game_view.isMarkingMode() ? R.string.uncover_mode : R.string.marking_mode);
    }

    private void updateGameInformation() {
        marked_mines_text.setText(getResources().getQuantityString(R.plurals.marked_mines, game_view.getMarkedMines(), game_view.getMarkedMines()));
        total_mines_text.setText(getResources().getString(R.string.total_mines, game_view.getTotalMines()));
    }
}
