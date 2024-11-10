import random
import collections

class Polynomial:
    """
    Polynomial y = a0 * x^0 + a1 * x^1 + ... + at * x^t on finite field Secp256k1.n
    """

    @staticmethod
    def random(order: int, debug: bool = False) -> 'Polynomial':
        """Random a polynomial with the specific order"""
        if order < 1:
            raise ValueError(f'The polynomial order should be a positive integer.')
        range_stop = curve.n #if debug else curve.n
        coefficients = [random.randrange(1, range_stop)]
        for i in range(order):
            coefficients.append(random.randrange(1, range_stop))
        return Polynomial(coefficients)

    @staticmethod
    def interpolate_evaluate(points: list, x: int) -> int:
        """Lagrange interpolate with the giving points, then evaluate y at x"""
        if len(points) < 2:
            raise ValueError('Lagrange interpolation requires at least 2 points')
        # [(numerator, denominator) ...]
        lagrange = [(0, 0)] * len(points)
        # the product of all the denominator
        denominator_product = 1
        for i in range(len(points)):
            numerator, denominator = 1, 1
            for j in range(len(points)):
                if j != i:
                    numerator *= (x - points[j][0])
                    denominator *= (points[i][0] - points[j][0])
            lagrange[i] = (points[i][1] * numerator, denominator)
            denominator_product *= denominator
        numerator_sum = 0
        for (numerator, denominator) in lagrange:
            numerator_sum += numerator * denominator_product // denominator
        return numerator_sum * modular_multiplicative_inverse(denominator_product, curve.n) % curve.n

    def __init__(self, coefficients: list) -> None:
        if len(coefficients) < 2:
            raise ValueError(f'The polynomial should have 2 coefficients at least.')
        self.order = len(coefficients) - 1
        self.coefficients = coefficients[:]

    def evaluate(self, x: int) -> int:
        """Calculate y for the specific x"""
        if x == 0:
            return self.coefficients[0]
        y, pow_x = 0, 1
        for i in range(len(self.coefficients)):
            y += self.coefficients[i] * pow_x
            pow_x *= x
        return y % curve.n

    def add(self, other: 'Polynomial') -> 'Polynomial':
        """Returns the polynomial = self + other"""
        coefficients = []
        i = 0
        while i < len(self.coefficients) and i < len(other.coefficients):
            coefficients.append(self.coefficients[i] + other.coefficients[i])
            i += 1
        while i < len(self.coefficients):
            coefficients.append(self.coefficients[i])
            i += 1
        while i < len(other.coefficients):
            coefficients.append(other.coefficients[i])
            i += 1
        for i in range(len(coefficients)):
            coefficients[i] %= curve.n
        return Polynomial(coefficients)

    def multiply(self, other: 'Polynomial') -> 'Polynomial':
        """Returns the polynomial = self * other"""
        coefficients = [0] * (self.order + other.order + 1)
        for i in range(len(self.coefficients)):
            for j in range(len(other.coefficients)):
                coefficients[i + j] += self.coefficients[i] * other.coefficients[j]
        for i in range(len(coefficients)):
            coefficients[i] %= curve.n
        return Polynomial(coefficients)

    def __str__(self) -> str:
        return f'<Polynomial order={self.order}, coefficients=[{", ".join(str(i) for i in self.coefficients)}]>'

    def __eq__(self, other: 'Polynomial') -> bool:
        return self.coefficients == other.coefficients

def extended_euclid_gcd(a: int, b: int) -> list:
    """
    Returns [gcd(a, b), x, y] where ax + by = gcd(a, b)
    """
    s, old_s = 0, 1
    t, old_t = 1, 0
    r, old_r = b, a
    while r != 0:
        quotient = old_r // r
        old_r, r = r, old_r - quotient * r
        old_s, s = s, old_s - quotient * s
        old_t, t = t, old_t - quotient * t
    return [old_r, old_s, old_t]


def modular_multiplicative_inverse(a: int, n: int) -> int:
    """
    Assumes that a and n are co-prime, returns modular multiplicative inverse of a under n
    """
    # Find gcd using Extended Euclid's Algorithm
    gcd, x, y = extended_euclid_gcd(a, n)
    # In case x is negative, we handle it by adding extra n
    # Because we know that modular multiplicative inverse of a in range n lies in the range [0, n-1]
    if x < 0:
        x += n
    return x


EllipticCurve = collections.namedtuple('EllipticCurve', 'name p a b g n h')

curve = EllipticCurve(
    name='Secp256k1',
    p=0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f,
    a=0,
    b=7,
    g=(0x79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798, 0x483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8),
    n=0xfffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141,
    h=1,
)


def on_curve(point: tuple) -> bool:
    """Returns True if the given point lies on the elliptic curve."""
    if point is None:
        # None represents the point at infinity.
        return True
    x, y = point
    return (y * y - x * x * x - curve.a * x - curve.b) % curve.p == 0


def negative(point: tuple) -> tuple or None:
    """Returns -point."""
    assert on_curve(point)
    if point is None:
        # -0 = 0
        return None
    x, y = point
    result = (x, -y % curve.p)
    assert on_curve(result)
    return result


def add(p: tuple, q: tuple) -> tuple or None:
    """Returns the result of p + q according to the group law."""
    assert on_curve(p)
    assert on_curve(q)
    if p is None:
        # 0 + q = q
        return q
    if q is None:
        # p + 0 = p
        return p
    #
    # p == -q
    #
    if p == negative(q):
        return None
    #
    # p != -q
    #
    x1, y1 = p
    x2, y2 = q
    if p == q:
        m = (3 * x1 * x1 + curve.a) * modular_multiplicative_inverse(2 * y1, curve.p)
    else:
        m = (y1 - y2) * modular_multiplicative_inverse(x1 - x2, curve.p)
    x = m * m - x1 - x2
    y = y1 + m * (x - x1)
    result = (x % curve.p, -y % curve.p)
    assert on_curve(result)
    return result


def scalar_multiply(k: int, point: tuple) -> tuple or None:
    """Returns k * point computed using the double and add algorithm."""
    assert on_curve(point)
    if k % curve.n == 0 or point is None:
        return None
    if k < 0:
        # k * point = -k * (-point)
        return scalar_multiply(-k, negative(point))
    # Double and add
    result = None
    while k:
        if k & 1:
            result = add(result, point)
        point = add(point, point)
        k >>= 1
    assert on_curve(result)
    return result



def generate_shares_and_public_key(group_size: int, threshold: int, debug: bool = True) -> tuple:
    #validate_parameters(group_size, threshold)
    polynomial_order = threshold - 1
    # Random polynomials for each player
    polynomials = [Polynomial.random(polynomial_order, debug) for _ in range(group_size)]
    if debug:
        for i, p in enumerate(polynomials):
            print(f'Player {i + 1} {p}')
    # Calculate shares for each player
    shares = [sum(p.evaluate(j + 1) for p in polynomials) % curve.n for j in range(group_size)]
    # Calculate group shared public key
    public_key = None
    for p in polynomials:
        public_key = add(public_key, scalar_multiply(p.coefficients[0], curve.g))
    if debug:
        secret = sum(p.coefficients[0] for p in polynomials) % curve.n
        mod_inv_secret = modular_multiplicative_inverse(secret, curve.n)
        print(f'secret = {secret}')
        print(f'mod_inv_secret = {mod_inv_secret}')
        print(f'public key = {public_key}')
        print(f'shares = {shares}')
    return shares, public_key, secret

def restore_key(shares: list,points: list, threshold: int) -> int:
    if len(shares) < threshold:
        raise ValueError('The number of shares is less than the threshold')
   # points = [(i + 1, share) for i, share in enumerate(shares)]
    return Polynomial.interpolate_evaluate(points, 0)

def restore_share_and_public_key(points: list, threshold: int) -> tuple:
    exist=[]
    exist.append(points[0][0])
    exist.append(points[1][0])
    full_list=[1,2,3]
    missing_number =list(set(full_list) - set(exist))[0]

    sk=Polynomial.interpolate_evaluate(points, 0)

    pk=scalar_multiply(sk,curve.g)
    sk=None

    return [missing_number,Polynomial.interpolate_evaluate(points, missing_number)],pk

# 封装一下restore_share_and_public_key,对外提供restoreShareAndPublicKey
def restoreShareAndPublicKey(shares: list, shareindexs: list,threshold: int) -> tuple:
    return restore_share_and_public_key(random.sample(shares_to_points(shares,shareindexs), threshold),threshold)

def restore_share(shares: list,points: list, threshold: int) -> int:
    if len(shares) < threshold:
        raise ValueError('The number of shares is less than the threshold')
   # points = [(i + 1, share) for i, share in enumerate(shares)]
    exist=[]
    exist.append(points[0][0])
    exist.append(points[1][0])
    full_list=[1,2,3]
    missing_number =list(set(full_list) - set(exist))[0]
    return [missing_number,Polynomial.interpolate_evaluate(points, missing_number)]

# 封装一下restore_share,对外提供restoreShare
def restoreShare(shares: list, shareindexs: list,threshold: int) -> int:
    return restore_share(shares,random.sample(shares_to_points(shares,shareindexs), threshold),threshold)

# 封装一下restore_key,对外提供restore
def restore(shares: list, shareindexs: list,threshold: int) -> int:
    return restore_key(shares,random.sample(shares_to_points(shares,shareindexs), threshold),threshold)

def shares_to_points(str_array: list,shareindexs: list) -> list:
    # str [] 转换成 int []
    shares = list(map(int, str_array))
    return [(shareindexs[i], share) for i, share in enumerate(shares)]

# def shares_to_points(str_array: list,shareindexs: list) -> list:
#     # str [] 转换成 int []
#     shares = list(map(int, str_array))
#     return [(i + 1, share) for i, share in enumerate(shares)]

def inspect(items: list) -> str:
    return f'[{", ".join(str(item) for item in items)}]'


def restore_share_and_public_key(points: list, threshold: int) -> int:
    exist=[]
    exist.append(points[0][0])
    exist.append(points[1][0])
    full_list=[1,2,3]
    missing_number =list(set(full_list) - set(exist))[0]

    sk=Polynomial.interpolate_evaluate(points, 0)

    pk=scalar_multiply(sk,curve.g) 
    sk=None

    return [missing_number,Polynomial.interpolate_evaluate(points, missing_number)],pk

