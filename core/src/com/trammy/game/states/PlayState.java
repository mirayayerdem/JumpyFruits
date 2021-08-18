package com.trammy.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.trammy.game.JumpyFruits;
import com.trammy.game.sprites.Fork;
import com.trammy.game.sprites.Character;

public class PlayState extends State
{
    private static final int KNIFE_SPACING = 125;
    private static final int KNIFE_COUNT = 4;
    private static final int GROUND_Y_OFFSET = -50;
    private Character character;
    private Texture bg;
    private String bgName = "";
    private Array<Fork> forks;
    private double oldForkPos = 0;
    private Texture ground;
    private int score;
    private Vector2 groundPos1, groundPos2;
    private Stage stage;
    private Texture pauseBtnTexture;


    public PlayState(GameStateManager gsm)
    {
        super(gsm);
        chooseBg(bgName);
        character = new Character(50,300, "");
        cam.setToOrtho(false, JumpyFruits.WIDTH / 2, JumpyFruits.HEIGHT / 2);
        forks = new Array<Fork>();
        score = 0;
        ground = new Texture("groundjf.png");
        groundPos1 = new Vector2(cam.position.x - cam.viewportWidth / 2, GROUND_Y_OFFSET);
        groundPos2 = new Vector2((cam.position.x - cam.viewportWidth / 2) + ground.getWidth(), GROUND_Y_OFFSET);

        for(int i = 1; i <= KNIFE_COUNT; i++)
        {
            forks.add(new Fork(i * (KNIFE_SPACING + Fork.KNIFE_WIDTH)));
        }
    }

    public PlayState(GameStateManager gsm, String charName, String bgName){
        super(gsm);
        this.bgName = bgName;
        chooseBg(bgName);
        character = new Character(50,300,charName);
        cam.setToOrtho(false, JumpyFruits.WIDTH / 2, JumpyFruits.HEIGHT / 2);
        forks = new Array<Fork>();
        score = 0;
        ground = new Texture("groundjf.png");
        groundPos1 = new Vector2(cam.position.x - cam.viewportWidth / 2, GROUND_Y_OFFSET);
        groundPos2 = new Vector2((cam.position.x - cam.viewportWidth / 2) + ground.getWidth(), GROUND_Y_OFFSET);

        for(int i = 1; i <= KNIFE_COUNT; i++)
        {
            forks.add(new Fork(i * (KNIFE_SPACING + Fork.KNIFE_WIDTH)));
        }
    }

    public void chooseBg(String bgName){
        if(bgName.equals(""))
            bg = new Texture("bg_grid.png");
        else
            bg = new Texture(bgName);
    }

    public void createButton(){
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        pauseBtnTexture = new Texture("pausebtn.png");
        Drawable pauseButtonDrawable = new TextureRegionDrawable(new TextureRegion(pauseBtnTexture));
        ImageButton pauseBtn = new ImageButton(pauseButtonDrawable);
        pauseBtn.setSize(pauseBtnTexture.getWidth() , pauseBtnTexture.getHeight());
        pauseBtn.setPosition(Gdx.graphics.getWidth() - pauseBtnTexture.getWidth() -25 ,Gdx.graphics.getHeight() - pauseBtnTexture.getHeight() - 25);
        pauseBtn.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("Press a Button");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                gsm.push(new PauseState(gsm, bgName));
                return true;
            }
        });
        stage.addActor(pauseBtn);


    }
    @Override
    public void handleInput()
    {
        if(Gdx.input.justTouched())
            character.jump();
    }


    @Override
    public void update ( float dt)
    {
        handleInput();
        updateGround();
        character.update(dt);
        cam.position.x = character.getPosition().x + 80;
        for (int i = 0; i < forks.size; i++)
        {
            Fork fork = forks.get(i);

            if( cam.position.x - (cam.viewportWidth / 2) > fork.getPosTopFork().x + fork.getTopFork().getWidth() )
                fork.reposition(fork.getPosTopFork().x + ((Fork.KNIFE_WIDTH + KNIFE_SPACING) * KNIFE_COUNT));


            if(character.getPosition().x >= (fork.getPosTopFork().x + fork.getTopFork().getWidth()) && oldForkPos != fork.getPosTopFork().x + fork.getTopFork().getWidth()  ) {
                addScore(1);
                oldForkPos = fork.getPosTopFork().x + fork.getTopFork().getWidth();

            }

            if(fork.collides(character.getBounds()))
                gsm.set(new PlayState(gsm));
        }

        if (character.getPosition().y <= ground.getHeight() + GROUND_Y_OFFSET)
            gsm.set(new PlayState(gsm));
        cam.update();

    }

    @Override
    public void render(SpriteBatch sb)
    {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(bg, cam.position.x - (cam.viewportWidth / 2), 0);
        sb.draw(character.getTexture(), character.getPosition().x, character.getPosition().y);
        sb.draw(ground, groundPos1.x, groundPos1.y);
        sb.draw(ground, groundPos2.x, groundPos2.y);
        for ( Fork fork : forks )
        {
            sb.draw(fork.getTopFork(), fork.getPosTopFork().x, fork.getPosTopFork().y);
            sb.draw(fork.getBottomFork(), fork.getPosBotFork().x, fork.getPosBotFork().y);
        }
        JumpyFruits.font.draw(sb, ""+ score +"", cam.position.x , cam.position.y * 7 / 4);
        sb.end();
        createButton();
        stage.act();
        stage.draw();

    }

    @Override
    public void dispose()
    {
        bg.dispose();
        character.dispose();
        ground.dispose();
        for( Fork fork : forks )
            fork.dispose();
        System.out.println("Play State Disposed");
        stage.dispose();
    }

    private void updateGround()
    {
        if(cam.position.x - cam.viewportWidth/2 > groundPos1.x + ground.getWidth())
            groundPos1.add(ground.getWidth() * 2 ,0);
        if(cam.position.x - cam.viewportWidth/2 > groundPos2.x + ground.getWidth())
            groundPos2.add(ground.getWidth() * 2 ,0);
    }

    public void addScore(int value){
        score += value;
    }

}

