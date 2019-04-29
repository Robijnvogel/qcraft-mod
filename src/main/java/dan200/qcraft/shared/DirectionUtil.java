package dan200.qcraft.shared;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;


public class DirectionUtil {

    // 0 -> Bottom
    // 1 -> Top
    // 2 -> North
    // 3 -> South
    // 4 -> West
    // 5 -> East
    // NOTE: This is not the same order as EnumFacing.ordinal()

    public static EnumFacing getFacing(int p_dir) {
        switch (p_dir) {
            case 0:
                return EnumFacing.DOWN;
            case 1:
                return EnumFacing.UP;
            case 2:
                return EnumFacing.NORTH;
            case 3:
                return EnumFacing.SOUTH;
            case 4:
                return EnumFacing.WEST;
            default:
                return EnumFacing.EAST;
        }
    }

    public static Vec3 transformVector(Vec3 p_vector, EnumFacing p_directionIn, EnumFacing p_directionOut) {
        Vec3 r_vector;

        if (p_directionIn == p_directionOut) {
            r_vector = Vec3.createVectorHelper(p_vector.xCoord, p_vector.yCoord, p_vector.zCoord);
            return r_vector;
        }

        if (facingsAreHorizontalOpposites(p_directionIn, p_directionOut)) {
            r_vector = Vec3.createVectorHelper(p_vector.xCoord * -1, p_vector.yCoord, p_vector.zCoord * -1);
            return r_vector;
        }

        if (facingsAreHorizontalClockWise(p_directionIn, p_directionOut)) {
            // Todo: This may need to be the other way around instead
            r_vector = Vec3.createVectorHelper(p_vector.zCoord * -1, p_vector.yCoord, p_vector.xCoord);
            return r_vector;
        }

        // Todo: And this may as well
        r_vector = Vec3.createVectorHelper(p_vector.zCoord, p_vector.yCoord, p_vector.xCoord * -1);
        return r_vector;
    }

    public static float transformYaw(float p_yaw, EnumFacing p_directionIn, EnumFacing p_directionOut) {
        if (p_directionIn == p_directionOut) {
            // If your entrance is the same as your exit, you need to exit facing the opposite way
            // Ie: You enter facing "inwards" and you exit facing "outwards"
            return (p_yaw + 180) % 360;
        }

        if (facingsAreHorizontalOpposites(p_directionIn, p_directionOut)) {
            return p_yaw;
        }

        if (facingsAreHorizontalClockWise(p_directionIn, p_directionOut)) {
            // Todo: This may need to be -90 instead
            return (p_yaw - 90) % 360;
        }

        // Todo: And this may need to be 90
        return (p_yaw + 90) % 360;
    }

    private static boolean facingsAreHorizontalOpposites(EnumFacing p_direction1, EnumFacing p_direction2) {

        return  (p_direction1 == EnumFacing.WEST && p_direction2 == EnumFacing.EAST) ||
                (p_direction1 == EnumFacing.EAST && p_direction2 == EnumFacing.WEST) ||
                (p_direction1 == EnumFacing.NORTH && p_direction2 == EnumFacing.SOUTH) ||
                (p_direction1 == EnumFacing.SOUTH && p_direction2 == EnumFacing.NORTH);
    }

    private static boolean facingsAreHorizontalClockWise(EnumFacing p_direction1, EnumFacing p_direction2) {

        return  (p_direction1 == EnumFacing.NORTH && p_direction2 == EnumFacing.EAST) ||
                (p_direction1 == EnumFacing.EAST && p_direction2 == EnumFacing.SOUTH) ||
                (p_direction1 == EnumFacing.SOUTH && p_direction2 == EnumFacing.WEST) ||
                (p_direction1 == EnumFacing.WEST && p_direction2 == EnumFacing.NORTH);
    }
}
