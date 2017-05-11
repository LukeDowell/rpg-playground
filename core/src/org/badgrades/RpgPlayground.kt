package org.badgrades

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.GridPoint2
import ktx.app.KotlinApplication
import ktx.app.KtxInputAdapter
import ktx.app.use
import ktx.collections.gdxArrayOf
import ktx.collections.gdxMapOf
import ktx.log.logger
class RpgPlayground : KotlinApplication(fixedTimeStep = 1f / 60f, maxDeltaTime = 1 / 15f), KtxInputAdapter {

    companion object {
        val log = logger<RpgPlayground>()
    }

    val unitScale = 1f / 32f

    lateinit var batch: SpriteBatch
    lateinit var tiledMap: TiledMap
    lateinit var tiledRenderer: OrthogonalTiledMapRenderer
    lateinit var camera: OrthographicCamera

    lateinit var preSpriteTiles: IntArray
    lateinit var postSpriteTiles: IntArray

    // Character
    val animationSpeed = 0.5f
    val animationMap = gdxMapOf<Direction, Animation<TextureRegion>>()
    var direction = Direction.RIGHT
    var stateTime = 0f
    var location = GridPoint2(0, 0)
    var isMoving: Boolean = false

    lateinit var characterAtlas: TextureAtlas

    override fun create() {
        Gdx.input.inputProcessor = this
        batch = SpriteBatch()
        tiledMap = TmxMapLoader().load("maps/test-lush.tmx")
        tiledRenderer = OrthogonalTiledMapRenderer(tiledMap, unitScale)
        characterAtlas = TextureAtlas(Gdx.files.internal("sprites/bob.atlas"))
        camera = OrthographicCamera()
        camera.setToOrtho(false, 60f, 32f)

        tiledMap.layers.forEachIndexed { index, mapLayer ->
            if (mapLayer.name.equals("player", ignoreCase = true)) {
                preSpriteTiles = (0..index).toList().toIntArray()

                try {
                    postSpriteTiles = (index + 1..tiledMap.layers.count - 1).toList().toIntArray()
                } catch (e: IndexOutOfBoundsException) {
                    postSpriteTiles = (index..tiledMap.layers.count - 1).toList().toIntArray()
                }
            }
        }

        val upAnimation = Animation<TextureRegion>(animationSpeed, characterAtlas.findRegions("bob-up"), Animation.PlayMode.LOOP)
        val downAnimation = Animation<TextureRegion>(animationSpeed, characterAtlas.findRegions("bob-down"), Animation.PlayMode.LOOP)
        val leftAnimation = Animation<TextureRegion>(animationSpeed, characterAtlas.findRegions("bob-left"), Animation.PlayMode.LOOP)
        val rightAnimation = Animation<TextureRegion>(animationSpeed, characterAtlas.findRegions("bob-right"), Animation.PlayMode.LOOP)

        animationMap.put(Direction.UP, upAnimation)
        animationMap.put(Direction.DOWN, downAnimation)
        animationMap.put(Direction.LEFT, leftAnimation)
        animationMap.put(Direction.RIGHT, rightAnimation)
    }

    override fun render(delta: Float) {
        val currentFrame = animationMap.get(direction).getKeyFrame(stateTime, true)

        camera.update()
        tiledRenderer.setView(camera)
        stateTime += Gdx.graphics.deltaTime

        if (isMoving) {
            location = GridPoint2(
                    location.x + (direction.offset.x * 5),
                    location.y + (direction.offset.y * 5)
            )
        }

        val pressedKeycode: Int? = gdxArrayOf(Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT)
                .filter { Gdx.input.isKeyPressed(it) }
                .firstOrNull()

        pressedKeycode?.let {
            when (it) {
                Input.Keys.LEFT -> { direction = Direction.LEFT }
                Input.Keys.RIGHT -> { direction = Direction.RIGHT }
                Input.Keys.DOWN -> { direction = Direction.DOWN }
                Input.Keys.UP -> { direction = Direction.UP }
            }

            location = GridPoint2(
                    location.x + (direction.offset.x * 2),
                    location.y + (direction.offset.y * 2)
            )
        }
        tiledRenderer.render(preSpriteTiles)
        batch.use {
            it.draw(currentFrame, location.x.toFloat(), location.y.toFloat())
        }
        tiledRenderer.render(postSpriteTiles)
    }

    override fun dispose() {
        batch.dispose()
        tiledRenderer.dispose()
        tiledMap.dispose()
    }
}

enum class Direction(val offset: GridPoint2) {
    UP(GridPoint2(0, 1)),
    DOWN(GridPoint2(0, -1)),
    LEFT(GridPoint2(-1, 0)),
    RIGHT(GridPoint2(1, 0));
}