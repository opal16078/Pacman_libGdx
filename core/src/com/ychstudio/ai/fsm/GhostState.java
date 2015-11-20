package com.ychstudio.ai.fsm;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.ychstudio.components.GhostComponent;
import com.ychstudio.gamesys.GameManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public enum GhostState implements State<GhostAgent> {

    MOVE_UP() {
        @Override
        public void update(GhostAgent entity) {
            entity.ghostComponent.currentState = GhostComponent.MOVE_UP;

            entity.ghostComponent.getBody().applyForceToCenter(0, entity.acceleration, true);
            if (entity.ghostComponent.getBody().getLinearVelocity().len2() > entity.speed * entity.speed) {
                entity.ghostComponent.getBody().setLinearVelocity(entity.ghostComponent.getBody().getLinearVelocity().scl(entity.speed / entity.ghostComponent.getBody().getLinearVelocity().len()));
            }

            if (checkHitWall(entity, GhostComponent.MOVE_UP)) {
                changeState(entity, getRandomDirectionChoice(getDirectionChoices(entity, GhostComponent.MOVE_DOWN)));
            }

            // TODO: make turing probability
            // check if there is way for turing every certain distance
            if (entity.timer > 0.5f) {
                entity.timer = 0;
                int newState = getRandomDirectionChoice(getDirectionChoices(entity, GhostComponent.MOVE_DOWN));
                if (newState != entity.ghostComponent.currentState) {
                    changeState(entity, newState);
                }
            }
        }

    },
    MOVE_DOWN() {
        @Override
        public void update(GhostAgent entity) {
            entity.ghostComponent.currentState = GhostComponent.MOVE_DOWN;

            entity.ghostComponent.getBody().applyForceToCenter(0, -entity.acceleration, true);
            if (entity.ghostComponent.getBody().getLinearVelocity().len2() > entity.speed * entity.speed) {
                entity.ghostComponent.getBody().setLinearVelocity(entity.ghostComponent.getBody().getLinearVelocity().scl(entity.speed / entity.ghostComponent.getBody().getLinearVelocity().len()));
            }

            if (checkHitWall(entity, GhostComponent.MOVE_DOWN)) {
                changeState(entity, getRandomDirectionChoice(getDirectionChoices(entity, GhostComponent.MOVE_UP)));
            }

            // TODO: make turing probability
            // check if there is way for turing every certain distance
            if (entity.timer > 0.5f) {
                entity.timer = 0;
                int newState = getRandomDirectionChoice(getDirectionChoices(entity, GhostComponent.MOVE_UP));
                if (newState != entity.ghostComponent.currentState) {
                    changeState(entity, newState);
                }
            }
        }
    },
    MOVE_LEFT() {
        @Override
        public void update(GhostAgent entity) {
            entity.ghostComponent.currentState = GhostComponent.MOVE_LEFT;

            entity.ghostComponent.getBody().applyForceToCenter(-entity.acceleration, 0, true);
            if (entity.ghostComponent.getBody().getLinearVelocity().len2() > entity.speed * entity.speed) {
                entity.ghostComponent.getBody().setLinearVelocity(entity.ghostComponent.getBody().getLinearVelocity().scl(entity.speed / entity.ghostComponent.getBody().getLinearVelocity().len()));
            }

            if (checkHitWall(entity, GhostComponent.MOVE_LEFT)) {
                changeState(entity, getRandomDirectionChoice(getDirectionChoices(entity, GhostComponent.MOVE_RIGHT)));
            }

            // TODO: make turing probability
            // check if there is way for turing every certain distance
            if (entity.timer > 0.5f) {
                entity.timer = 0;
                int newState = getRandomDirectionChoice(getDirectionChoices(entity, GhostComponent.MOVE_RIGHT));
                if (newState != entity.ghostComponent.currentState) {
                    changeState(entity, newState);
                }
            }
        }
    },
    MOVE_RIGHT() {
        @Override
        public void update(GhostAgent entity) {
            entity.ghostComponent.currentState = GhostComponent.MOVE_RIGHT;

            entity.ghostComponent.getBody().applyForceToCenter(entity.acceleration, 0, true);
            if (entity.ghostComponent.getBody().getLinearVelocity().len2() > entity.speed * entity.speed) {
                entity.ghostComponent.getBody().setLinearVelocity(entity.ghostComponent.getBody().getLinearVelocity().scl(entity.speed / entity.ghostComponent.getBody().getLinearVelocity().len()));
            }

            if (checkHitWall(entity, GhostComponent.MOVE_RIGHT)) {
                changeState(entity, getRandomDirectionChoice(getDirectionChoices(entity, GhostComponent.MOVE_LEFT)));
            }

            // TODO: make turing probability
            // check if there is way for turing every certain distance
            if (entity.timer > 0.5f) {
                entity.timer = 0;
                int newState = getRandomDirectionChoice(getDirectionChoices(entity, GhostComponent.MOVE_LEFT));
                if (newState != entity.ghostComponent.currentState) {
                    changeState(entity, newState);
                }
            }
        }
    },
    ESCAPE() {
        @Override
        public void update(GhostAgent entity) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    },
    DIE() {
        @Override
        public void update(GhostAgent entity) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    };

    private static void changeState(GhostAgent entity, int state) {
        switch (state) {
            case 0: // UP
                entity.stateMachine.changeState(MOVE_UP);
                break;
            case 1: // DOWN
                entity.stateMachine.changeState(MOVE_DOWN);
                break;
            case 2: // LEFT
                entity.stateMachine.changeState(MOVE_LEFT);
                break;
            case 3: // RIGHT
                entity.stateMachine.changeState(MOVE_RIGHT);
                break;
            case 4: // ESCAPE
                entity.stateMachine.changeState(ESCAPE);
                break;
            case 5: // DIE
                entity.stateMachine.changeState(DIE);
                break;
            default:
                break;
        }
    }

    private static final Vector2 tmpV1 = new Vector2();
    private static final Vector2 tmpV2 = new Vector2();
    private static final List<Integer> choicesList = new ArrayList<>(4);
    private static boolean hitWall = false;

    private static final float radius = 0.55f;

    private static boolean checkHitWall(GhostAgent entity, int state) {
        Body body = entity.ghostComponent.getBody();
        World world = body.getWorld();
        hitWall = false;

        tmpV1.set(body.getWorldCenter());

        switch (state) {
            case 0: // UP
                tmpV2.set(tmpV1).add(0, radius);
                break;
            case 1: // DOWN
                tmpV2.set(tmpV1).add(0, -radius);
                break;
            case 2: // LEFT
                tmpV2.set(tmpV1).add(-radius, 0);
                break;
            case 3: // RIGHT
                tmpV2.set(tmpV1).add(radius, 0);
                break;
            default:
                tmpV2.setZero();
                break;
        }
        world.rayCast(rayCastCallback, tmpV1, tmpV2);

        return hitWall;
    }

    private static Integer[] getDirectionChoices(GhostAgent entity, int state) {
        Body body = entity.ghostComponent.getBody();
        World world = body.getWorld();

        choicesList.clear();
        for (int i = 0; i < 4; i++) {
            choicesList.add(i);
        }

        choicesList.remove(state);

        tmpV1.set(body.getWorldCenter());

        Iterator<Integer> itor = choicesList.iterator();
        while (itor.hasNext()) {
            Integer integer = itor.next();

            hitWall = false;
            switch (integer) {
                case 0: // UP
                    tmpV2.set(tmpV1).add(0, radius);
                    break;
                case 1: // DOWN
                    tmpV2.set(tmpV1).add(0, -radius);
                    break;
                case 2: // LEFT
                    tmpV2.set(tmpV1).add(-radius, 0);
                    break;
                case 3: // RIGHT
                    tmpV2.set(tmpV1).add(radius, 0);
                    break;
                default:
                    tmpV2.setZero();
                    break;
            }

            world.rayCast(rayCastCallback, tmpV1, tmpV2);
            if (hitWall) {
                itor.remove();
            }
        }

        Integer[] result = choicesList.toArray(new Integer[choicesList.size()]);

        return result;
    }

    private static RayCastCallback rayCastCallback = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if (fixture.getFilterData().categoryBits == GameManager.WALL_BIT) {
                hitWall = true;
                return 0;
            }
            return 0;
        }
    };

    private static int getRandomDirectionChoice(Integer[] choices) {
        if (choices.length == 0) {
            return 0;
        }
        int length = choices.length;
        return choices[MathUtils.random(length - 1)];
    }

    @Override
    public void enter(GhostAgent entity) {
        entity.ghostComponent.getBody().setLinearVelocity(0, 0);
        entity.timer = 0;
    }

    @Override
    public void exit(GhostAgent entity) {
    }

    @Override
    public boolean onMessage(GhostAgent entity, Telegram telegram) {
        return false;
    }

}
