# 직접 만들어보는 ArrayList (array 패키지)

배열(Array)의 특징에서 출발해서, 자바 `ArrayList`와 유사한 자료구조를 단계별(V1 → V4)로 직접 구현해 나가는 과정을 정리한 문서다.
각 단계는 `MyArrayListVx` (구현) 와 `MyArrayListVxMain` (사용 예시) 으로 구성되어 있고,
**"이 단계에서 무엇을 했고, 어떤 문제가 남아서 다음 버전으로 넘어갔는가"** 를 중심으로 설명한다.

---

## 0단계. 배열의 기초 — `ArrayMain1`, `ArrayMain2`

ArrayList를 만들기 전에, 그 밑바탕이 되는 **배열(Array)** 의 성능 특성을 먼저 이해한다.

### `ArrayMain1` — 배열의 기본 연산과 시간 복잡도
| 연산 | 예시 | 시간 복잡도 |
|------|------|:----------:|
| index로 입력 | `arr[0] = 1` | O(1) |
| index로 변경 | `arr[2] = 10` | O(1) |
| index로 조회 | `arr[2]` | O(1) |
| 값 검색 | 반복문으로 값 찾기 | O(n) |

- 배열은 **인덱스를 알면** 입력·변경·조회가 O(1)로 매우 빠르다.
- 반면 특정 **값을 검색**하려면 처음부터 끝까지 순회해야 하므로 O(n)이다.

### `ArrayMain2` — 배열에 "추가"할 때의 문제
- `addLast` (맨 뒤에 추가): O(1) — 빈 자리에 바로 넣으면 된다.
- `addFirst` (맨 앞에 추가): O(n) — 기존 데이터를 **한 칸씩 뒤로 밀어야** 한다.
- `addAtIndex` (중간에 추가): O(n) — index 뒤쪽 데이터를 모두 밀어야 한다.

> **핵심 문제 제기**: 배열은 조회는 빠르지만,
> ① 크기가 고정되어 있고 ② 중간 삽입/삭제 시 데이터를 밀어야 한다.
> 또한 사용자가 직접 index와 size를 관리해야 해서 불편하다.
> → 이 불편함을 감싸주는 **자료구조(List)** 를 직접 만들어보자.

---

## V1. 배열을 감싼 기본 리스트 — `MyArrayListV1`

배열을 필드로 감추고, `add` / `get` / `set` / `indexOf` / `size` 기능을 메서드로 제공한다.

```java
private Object[] elementData;   // 실제 데이터를 담는 배열
private int size = 0;           // 현재 데이터 개수
```

- `Object[]` 를 사용해 **어떤 타입이든** 담을 수 있게 했다.
- `size` 를 내부에서 자동 관리하므로, 사용자는 index를 직접 신경 쓰지 않아도 된다.
- `add(e)` 는 항상 마지막(`size` 위치)에 추가 → O(1).
- `toString()` 으로 `size`, `capacity`(배열 크기)를 함께 출력해 상태를 확인할 수 있다.

### ⚠️ V1의 문제점
`MyArrayListV1Main` 의 마지막 부분을 보면:

```
==범위 초과==
list.add("d");  // OK
list.add("e");  // OK (capacity 5 꽉 참)
list.add("f");  // ArrayIndexOutOfBoundsException 발생!
```

- 내부 배열의 크기(`DEFAULT_CAPACITY = 5`)가 **고정**이다.
- 데이터가 capacity를 넘어가면 **배열 범위 초과 예외**가 발생한다.

> **→ 해결 과제**: 배열이 꽉 차면 자동으로 크기를 늘려야 한다. (V2)

---

## V2. 동적 크기 증가 (grow) — `MyArrayListV2`

배열이 가득 찼을 때 **더 큰 배열로 자동 확장**하는 기능을 추가했다.

```java
public void add(Object e) {
    if (size == elementData.length) {  // 배열이 꽉 찼으면
        grow();                        // 크기를 늘린다
    }
    elementData[size] = e;
    size++;
}

private void grow() {
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity * 2;   // 2배로 확장
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```

- `add` 시점에 배열이 가득 찼는지 검사하고, 가득 찼으면 `grow()` 호출.
- `Arrays.copyOf` 로 **2배 크기의 새 배열**에 기존 데이터를 복사한다.
- `MyArrayListV2Main` 은 capacity 2로 시작해 데이터를 계속 넣으며 `2 → 4 → 8` 로 늘어나는 것을 보여준다.

> V1의 "범위 초과 예외" 문제가 해결되었다. 이제 무한정 추가할 수 있다.

### ⚠️ V2의 한계
- 추가는 오직 **맨 뒤에만(addLast)** 가능하다.
- **원하는 위치에 삽입**하거나 **삭제**하는 기능이 아직 없다.

> **→ 해결 과제**: 중간 삽입(add(index, e))과 삭제(remove(index))를 추가하자. (V3)

---

## V3. 중간 삽입 · 삭제 기능 추가 — `MyArrayListV3`

특정 index에 삽입/삭제하는 기능을 추가했다. 이때 **데이터를 밀어야** 한다(0단계 배열의 특징과 연결됨).

### 추가된 기능
```java
// 특정 위치에 삽입
public void add(int index, Object e) {
    if (size == elementData.length) grow();
    shiftRightFrom(index);   // index부터 오른쪽으로 한 칸씩 밀기
    elementData[index] = e;
    size++;
}

// 특정 위치 삭제
public Object remove(int index) {
    Object oldValue = get(index);
    shiftLeftFrom(index);    // index부터 왼쪽으로 한 칸씩 당기기
    size--;
    elementData[size] = null; // 마지막 참조 제거 (메모리 정리)
    return oldValue;
}
```

- `shiftRightFrom` / `shiftLeftFrom` 헬퍼로 데이터를 밀거나 당긴다.
- `remove` 후 마지막 칸을 `null` 로 비워, 쓰지 않는 객체 참조를 제거한다.

### 성능 특성 (`MyArrayListV3Main`)
| 연산 | 위치 | 복잡도 |
|------|------|:------:|
| add | 마지막 | O(1) |
| add | 처음/중간 | O(n) — 뒤로 밀어야 함 |
| remove | 마지막 | O(1) |
| remove | 처음/중간 | O(n) — 앞으로 당겨야 함 |

### ⚠️ V3의 문제점 — 타입 안전성 (`MyArrayListV3BadMain`)
내부가 `Object[]` 이다 보니 **아무 타입이나 섞여 들어갈 수 있다.**

```java
MyArrayListV3 numberList = new MyArrayListV3();
numberList.add(1);
numberList.add(2);
numberList.add("문자3");   // 숫자만 넣으려 했지만 문자도 들어감 (컴파일 OK)

Integer num1 = (Integer) numberList.get(0); // 매번 다운캐스팅 필요
Integer num3 = (Integer) numberList.get(2); // ClassCastException 발생! (런타임 오류)
```

두 가지 불편함/위험:
1. `get()` 이 `Object` 를 반환하므로 **꺼낼 때마다 다운캐스팅**을 해야 한다.
2. 잘못된 타입이 섞여도 컴파일 시점에 못 잡고, **런타임에 `ClassCastException`** 으로 터진다.

> **→ 해결 과제**: 제네릭(Generic)을 도입해 컴파일 시점에 타입을 보장하자. (V4)

---

## V4. 제네릭 도입 — `MyArrayListV4<E>`

클래스에 **타입 매개변수 `<E>`** 를 도입해, 담을 타입을 생성 시점에 지정하도록 했다.

```java
public class MyArrayListV4<E> {
    public void add(E e) { ... }          // E 타입만 받음
    public E get(int index) {             // E 타입으로 반환 (캐스팅 불필요)
        return (E) elementData[index];
    }
    public E set(int index, E element) { ... }
    public E remove(int index) { ... }
    public int indexOf(E o) { ... }
}
```

### V3의 문제가 어떻게 해결되는가
- **타입 안전성 (컴파일 시점)**: `MyArrayListV4<Integer>` 로 만들면 `add("문자")` 는 **컴파일 에러** → 잘못된 타입이 애초에 들어갈 수 없다.
- **다운캐스팅 불필요**: `get()` 이 `E` 를 반환하므로 `Integer num = list.get(0);` 처럼 바로 받을 수 있다.
- 내부 저장소는 여전히 `Object[]` 지만, `get` 에서 `(E)` 캐스팅을 하며 `@SuppressWarnings("unchecked")` 로 경고를 억제한다. (제네릭 배열의 일반적인 구현 방식)

> 이렇게 해서 **자바의 `ArrayList<E>` 와 유사한 형태**의 자료구조가 완성되었다.

---

## 전체 흐름 요약

| 버전 | 한 일 | 남은 문제 → 다음 단계 |
|:----:|-------|----------------------|
| **배열** | 배열의 성능 특성 이해 (조회 O(1), 삽입 O(n), 크기 고정) | 크기 고정 + 직접 index/size 관리 불편 |
| **V1** | 배열을 감싼 리스트, size 자동 관리, add/get/set/indexOf | capacity 초과 시 예외 발생 |
| **V2** | `grow()` 로 배열 자동 2배 확장 | addLast만 가능, 삽입/삭제 없음 |
| **V3** | 중간 삽입 `add(index, e)`, 삭제 `remove(index)` | `Object[]` 라 타입 불안전 + 다운캐스팅 필요 |
| **V4** | 제네릭 `<E>` 도입 | 타입 안전 + 캐스팅 제거 → **ArrayList 완성** |

### 핵심 교훈
- **ArrayList = 배열 + α**: 조회가 빠른 배열의 장점은 유지하면서, 크기 자동 확장과 편의 기능을 얹은 자료구조다.
- **삽입/삭제의 비용**: 맨 뒤는 O(1)이지만, 중간은 데이터 이동 때문에 O(n)이다. 이 특성이 이후 `LinkedList` 를 배우는 동기가 된다.
- **제네릭의 가치**: 런타임 오류(`ClassCastException`)를 컴파일 시점 오류로 앞당겨 잡아준다.
