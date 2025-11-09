# Hyprtoolkit API Compatibility Notes

## Issues with Hyprtoolkit 0.2.1

The current code was written based on Hyprtoolkit documentation, but the actual 0.2.1 API has several breaking changes:

### API Differences Found

1. **CFontSize enum**:
   - Tried: `CFontSize::HT_SIZE_BASE_PX`, `CFontSize::HT_SIZE_PX`
   - Actual: Need to check actual header for correct enum name

2. **CDynamicSize constructor**:
   - Takes 3 arguments: `(eSizingType typeX, eSizingType typeY, const Vector2D& size)`
   - Fixed ✅

3. **setMouseButton signature**:
   - Takes: `(Input::eMouseButton, bool)` - no position parameter
   - Mouse position not available in callback
   - Fixed ✅

4. **setMouseEnter signature**:
   - Takes: `(const Vector2D&)` as parameter (mouse position)
   - Partially fixed ⚠️

5. **addTimer signature**:
   - Takes 4 parameters: `(duration, callback_with_timer_ptr, void* data, bool)`
   - Was: `(duration, simple_callback)`
   - Needs fix ❌

6. **Window close event**:
   - `m_events.closeRequest` is a Signal, not a function pointer
   - Can't directly assign lambda
   - Need to use `.registerListener()` or similar
   - Needs fix ❌

7. **Smart pointers**:
   - Uses `Hyprutils::Memory::CSharedPointer` not `std::shared_ptr`
   - Fixed ✅

8. **setMargin**:
   - Only takes single float, not 4 separate values
   - Fixed ✅ (using top value only)

### Required Header Dependencies

- libdrm (`drm_fourcc.h`)
- pixman-1
- hyprutils
- hyprtoolkit

All added to CMakeLists.txt ✅

### Next Steps

1. **Check actual headers** for correct enum names:
   ```bash
   grep -r "HT_SIZE" /usr/include/hyprtoolkit/
   ```

2. **Fix addTimer** - check header for correct signature

3. **Fix closeRequest signal** - use proper signal API

4. **Consider version check** - may need to support multiple versions

### Workaround for POC

For a quick POC, could:
- Use default font size (skip fontSize call)
- Skip timer functionality
- Skip close callback
- This would at least let us test basic UI

### Alternative: Target Specific Version

Could document that hyprclj requires hyprtoolkit >= 0.2.4 (or whatever version stabilizes the API).
