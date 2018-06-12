package original.stats;

class MomentInertia {
    public static double centerUniformRod(double length, double mass) {
        return mass * length * length / 12;
    }
    
    public static double sphere(double length, double mass) {
        return mass * length * length / 12;
    }
    
    public MomentInertia() { super(); }
}
